package com.example.healthcareapppd.data.health

import android.content.Context
import android.util.Log
import com.example.healthcareapppd.utils.TokenManager
import com.example.healthcareapppd.domain.usecase.patient.AddHealthMetricsUseCase
import com.example.healthcareapppd.domain.usecase.patient.AddVitalsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

/**
 * MOCK GOOGLE FIT DATA - ĐỂ TEST KHÔNG CẦN THIẾT BỊ THẬT
 * 
 * Tạo dữ liệu giả lập từ Google Fit để test API integration
 */
class MockGoogleFitData(
    private val context: Context,
    private val tokenManager: TokenManager,
    private val addHealthMetricsUseCase: AddHealthMetricsUseCase,
    private val addVitalsUseCase: AddVitalsUseCase
) {
    
    companion object {
        private const val TAG = "MockGoogleFit"
    }
    
    /**
     * Tạo và upload dữ liệu mock cho 7 ngày gần đây
     */
    suspend fun generateAndUploadMockData(onProgress: (String) -> Unit) {
        val token = tokenManager.getToken() ?: run {
            onProgress("❌ Chưa đăng nhập")
            return
        }
        
        onProgress("🔄 Bắt đầu tạo mock data...")
        delay(500)
        
        val calendar = Calendar.getInstance()
        
        // Tạo data cho 7 ngày gần đây
        for (dayOffset in 6 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_MONTH, -dayOffset)
            
            val dateStr = SimpleDateFormat("dd/MM", Locale.getDefault()).format(calendar.time)
            onProgress("📅 Đang tạo data cho ngày $dateStr...")
            
            // Tạo data cho ngày này
            generateDayData(calendar, token, onProgress)
            
            delay(300)
        }
        
        // Tạo vitals mới nhất (hôm nay)
        onProgress("💊 Đang tạo vitals...")
        generateLatestVitals(token, onProgress)
        delay(300)
        
        onProgress("✅ Hoàn thành! Đã tạo mock data cho 7 ngày")
    }
    
    /**
     * Tạo dữ liệu cho 1 ngày
     */
    private suspend fun generateDayData(calendar: Calendar, token: String, onProgress: (String) -> Unit) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        
        // Start time: 00:00
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = dateFormat.format(calendar.time)
        
        // End time: 23:59
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endTime = dateFormat.format(calendar.time)
        
        // 1. Số bước chân (5,000 - 15,000)
        val steps = Random.nextInt(5000, 15000).toDouble()
        uploadMetric(token, "steps", steps, startTime, endTime)
        
        // 2. Calories tiêu hao (1,500 - 3,000 kcal)
        val calories = Random.nextInt(1500, 3000).toDouble()
        uploadMetric(token, "active_calories", calories, startTime, endTime)
        
        // 3. Quãng đường (3,000 - 10,000 mét)
        val distance = Random.nextInt(3000, 10000).toDouble()
        uploadMetric(token, "distance_meters", distance, startTime, endTime)
        
        // 4. Giấc ngủ (360 - 540 phút = 6-9 giờ)
        // Sleep time: từ 22:00 ngày hôm trước đến 06:00 ngày hôm sau
        calendar.set(Calendar.HOUR_OF_DAY, 22)
        calendar.set(Calendar.MINUTE, 0)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val sleepStart = dateFormat.format(calendar.time)
        
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 6)
        calendar.set(Calendar.MINUTE, Random.nextInt(0, 60))
        val sleepEnd = dateFormat.format(calendar.time)
        
        val sleepMinutes = Random.nextInt(360, 540).toDouble()
        uploadMetric(token, "sleep_duration_minutes", sleepMinutes, sleepStart, sleepEnd)
    }
    
    /**
     * Tạo vitals mới nhất
     */
    private suspend fun generateLatestVitals(token: String, onProgress: (String) -> Unit) {
        // Chiều cao: 155-185 cm
        val height = Random.nextDouble(155.0, 185.0)
        
        // Cân nặng: 45-90 kg (dựa vào chiều cao)
        val idealWeight = (height - 100) * 0.9
        val weight = idealWeight + Random.nextDouble(-10.0, 15.0)
        
        // Nhịp tim: 60-100 bpm (bình thường)
        val heartRate = Random.nextInt(60, 100)
        
        // Huyết áp: 110-130 / 70-85 mmHg
        val systolic = Random.nextInt(110, 130)
        val diastolic = Random.nextInt(70, 85)
        
        // Nhiệt độ: 36.0 - 37.2°C
        val temperature = Random.nextDouble(36.0, 37.2)
        
        // Đường huyết: 70-100 mg/dL (bình thường)
        val glucose = Random.nextInt(70, 100)
        
        // SpO2: 95-100%
        val oxygen = Random.nextInt(95, 100)
        
        CoroutineScope(Dispatchers.IO).launch {
            val result = addVitalsUseCase(
                token = token,
                heightCm = height,
                weightKg = weight,
                heartRateBpm = heartRate,
                bloodPressureSystolic = systolic,
                bloodPressureDiastolic = diastolic,
                temperatureCelsius = temperature,
                bloodGlucoseMgDl = glucose,
                oxygenSaturationPercent = oxygen
            )
            
            result.onSuccess {
                Log.d(TAG, "✅ Vitals uploaded: BMI=${weight/((height/100)*(height/100))}")
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to upload vitals: ${error.message}")
            }
        }
    }
    
    /**
     * Upload metric lên server
     */
    private suspend fun uploadMetric(
        token: String,
        metricType: String,
        value: Double,
        startTime: String,
        endTime: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = addHealthMetricsUseCase(
                token = token,
                metricType = metricType,
                value = value,
                startTime = startTime,
                endTime = endTime
            )
            
            result.onSuccess {
                Log.d(TAG, "✅ Uploaded $metricType: $value")
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to upload $metricType: ${error.message}")
            }
        }
    }
    
    /**
     * Tạo dữ liệu hôm nay (realtime simulation)
     */
    suspend fun generateTodayData(onProgress: (String) -> Unit) {
        val token = tokenManager.getToken() ?: return
        
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        
        // Start: 00:00 hôm nay
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = dateFormat.format(calendar.time)
        
        // End: hiện tại
        val endTime = dateFormat.format(Date())
        
        // Tính số giờ đã trôi qua trong ngày
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val progress = currentHour / 24f
        
        // Số bước tương ứng với thời gian (max 12,000 bước/ngày)
        val steps = (12000 * progress).toInt()
        onProgress("👣 $steps bước")
        uploadMetric(token, "steps", steps.toDouble(), startTime, endTime)
        delay(200)
        
        // Calories
        val calories = (2500 * progress).toInt()
        onProgress("🔥 $calories kcal")
        uploadMetric(token, "active_calories", calories.toDouble(), startTime, endTime)
        delay(200)
        
        // Quãng đường
        val distance = (8000 * progress).toInt()
        onProgress("🚶 ${distance}m")
        uploadMetric(token, "distance_meters", distance.toDouble(), startTime, endTime)
        delay(200)
        
        onProgress("✅ Đã cập nhật dữ liệu hôm nay")
    }
}

/**
 * Extension function để dễ dùng
 */
suspend fun Context.generateMockHealthData(
    onProgress: (String) -> Unit = {},
    todayOnly: Boolean = false
) {
    val mockData = MockGoogleFitData(
        context = this,
        tokenManager = TokenManager(this),
        addHealthMetricsUseCase = AddHealthMetricsUseCase(),
        addVitalsUseCase = AddVitalsUseCase()
    )
    
    if (todayOnly) {
        mockData.generateTodayData(onProgress)
    } else {
        mockData.generateAndUploadMockData(onProgress)
    }
}
