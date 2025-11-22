package com.example.healthcareapppd.data.health

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.example.healthcareapppd.utils.TokenManager
import com.example.healthcareapppd.domain.usecase.patient.AddHealthMetricsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class để đọc số bước chân từ cảm biến điện thoại
 * 
 * CÁCH SỬ DỤNG:
 * 
 * 1. Thêm permission vào AndroidManifest.xml:
 * ```xml
 * <uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
 * <uses-feature android:name="android.hardware.sensor.stepcounter" android:required="false" />
 * ```
 * 
 * 2. Request permission runtime (Android 10+):
 * ```kotlin
 * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
 *     requestPermissions(
 *         arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
 *         ACTIVITY_RECOGNITION_REQUEST_CODE
 *     )
 * }
 * ```
 * 
 * 3. Sử dụng trong Fragment/Activity:
 * ```kotlin
 * private lateinit var stepCounterHelper: StepCounterHelper
 * 
 * override fun onCreate(savedInstanceState: Bundle?) {
 *     super.onCreate(savedInstanceState)
 *     
 *     stepCounterHelper = StepCounterHelper(
 *         context = this,
 *         tokenManager = TokenManager(this),
 *         addHealthMetricsUseCase = AddHealthMetricsUseCase()
 *     )
 * }
 * 
 * override fun onResume() {
 *     super.onResume()
 *     stepCounterHelper.startCounting { steps ->
 *         // Cập nhật UI với số bước
 *         tvSteps.text = "$steps bước"
 *     }
 * }
 * 
 * override fun onPause() {
 *     super.onPause()
 *     stepCounterHelper.stopCounting()
 * }
 * ```
 */
class StepCounterHelper(
    private val context: Context,
    private val tokenManager: TokenManager,
    private val addHealthMetricsUseCase: AddHealthMetricsUseCase
) : SensorEventListener {
    
    companion object {
        private const val TAG = "StepCounterHelper"
        private const val PREFS_NAME = "step_counter_prefs"
        private const val KEY_INITIAL_STEPS = "initial_steps"
        private const val KEY_LAST_SAVED_DATE = "last_saved_date"
    }
    
    private val sensorManager: SensorManager = 
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    private var stepCounterSensor: Sensor? = null
    private var initialSteps = 0L
    private var currentSteps = 0L
    private var onStepUpdate: ((Int) -> Unit)? = null
    
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    init {
        // Kiểm tra xem thiết bị có hỗ trợ cảm biến bước chân không
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        
        if (stepCounterSensor == null) {
            Log.w(TAG, "Step counter sensor not available on this device")
        } else {
            Log.d(TAG, "Step counter sensor available: ${stepCounterSensor?.name}")
        }
        
        // Load initial steps from last session
        initialSteps = prefs.getLong(KEY_INITIAL_STEPS, 0L)
    }
    
    /**
     * Bắt đầu đếm bước chân
     * @param onUpdate callback để cập nhật UI với số bước hiện tại
     */
    fun startCounting(onUpdate: (Int) -> Unit) {
        this.onStepUpdate = onUpdate
        
        stepCounterSensor?.let { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )
            Log.d(TAG, "Started counting steps")
        } ?: run {
            Log.e(TAG, "Cannot start counting - sensor not available")
            onUpdate(0)
        }
    }
    
    /**
     * Dừng đếm bước chân
     */
    fun stopCounting() {
        sensorManager.unregisterListener(this)
        Log.d(TAG, "Stopped counting steps")
        
        // Lưu dữ liệu lên server
        saveDailyStepsToServer()
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                currentSteps = it.values[0].toLong()
                
                // Lần đầu tiên đọc giá trị
                if (initialSteps == 0L) {
                    initialSteps = currentSteps
                    prefs.edit().putLong(KEY_INITIAL_STEPS, initialSteps).apply()
                }
                
                // Tính số bước từ khi bắt đầu app
                val stepsToday = (currentSteps - initialSteps).toInt()
                
                // Callback để update UI
                onStepUpdate?.invoke(stepsToday)
                
                Log.d(TAG, "Steps: $stepsToday (Total: $currentSteps, Initial: $initialSteps)")
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Sensor accuracy changed: $accuracy")
    }
    
    /**
     * Lưu số bước chân hôm nay lên server
     */
    private fun saveDailyStepsToServer() {
        val token = tokenManager.getToken() ?: return
        val stepsToday = (currentSteps - initialSteps).toInt()
        
        if (stepsToday <= 0) return
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        
        // Thời gian bắt đầu ngày (00:00)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = dateFormat.format(calendar.time)
        
        // Thời gian hiện tại
        val endTime = dateFormat.format(Date())
        
        CoroutineScope(Dispatchers.IO).launch {
            val result = addHealthMetricsUseCase(
                token = token,
                metricType = "steps",
                value = stepsToday.toDouble(),
                startTime = startTime,
                endTime = endTime
            )
            
            result.onSuccess {
                Log.d(TAG, "Saved $stepsToday steps to server")
                
                // Lưu ngày đã save
                prefs.edit().putString(KEY_LAST_SAVED_DATE, getCurrentDate()).apply()
            }.onFailure { error ->
                Log.e(TAG, "Failed to save steps: ${error.message}")
            }
        }
    }
    
    /**
     * Reset bộ đếm vào đầu ngày mới
     */
    fun checkAndResetForNewDay() {
        val today = getCurrentDate()
        val lastSavedDate = prefs.getString(KEY_LAST_SAVED_DATE, "")
        
        if (today != lastSavedDate) {
            // Ngày mới - reset initial steps
            initialSteps = currentSteps
            prefs.edit()
                .putLong(KEY_INITIAL_STEPS, initialSteps)
                .putString(KEY_LAST_SAVED_DATE, today)
                .apply()
            
            Log.d(TAG, "Reset for new day: $today")
        }
    }
    
    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    
    /**
     * Kiểm tra xem thiết bị có hỗ trợ cảm biến bước chân không
     */
    fun isStepCounterAvailable(): Boolean {
        return stepCounterSensor != null
    }
}
