package com.example.healthcareapppd.data.health

import android.content.Context
import android.util.Log
import com.example.healthcareapppd.utils.TokenManager
import com.example.healthcareapppd.domain.usecase.patient.AddHealthMetricsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class để đồng bộ dữ liệu từ Google Fit API
 * 
 * HƯỚNG DẪN TÍCH HỢP:
 * 
 * 1. Thêm dependencies vào build.gradle:
 * ```
 * implementation 'com.google.android.gms:play-services-fitness:21.1.0'
 * implementation 'com.google.android.gms:play-services-auth:20.7.0'
 * ```
 * 
 * 2. Thêm permissions vào AndroidManifest.xml:
 * ```xml
 * <uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
 * ```
 * 
 * 3. Khai báo trong Google Cloud Console:
 * - Tạo OAuth 2.0 credentials
 * - Enable Fitness API
 * - Thêm SHA-1 fingerprint
 * 
 * 4. Request permissions:
 * ```kotlin
 * val fitnessOptions = FitnessOptions.builder()
 *     .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
 *     .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
 *     .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
 *     .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
 *     .build()
 * 
 * GoogleSignIn.requestPermissions(
 *     this, 
 *     GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, 
 *     GoogleSignIn.getAccountForExtension(this, fitnessOptions),
 *     fitnessOptions
 * )
 * ```
 */
class GoogleFitHelper(
    private val context: Context,
    private val tokenManager: TokenManager,
    private val addHealthMetricsUseCase: AddHealthMetricsUseCase
) {
    
    companion object {
        private const val TAG = "GoogleFitHelper"
        const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1001
    }
    
    /**
     * Đồng bộ số bước chân từ Google Fit
     * 
     * Example code (uncomment khi đã add dependencies):
     * ```
     * suspend fun syncStepsFromGoogleFit(startTime: Long, endTime: Long) {
     *     val fitnessOptions = FitnessOptions.builder()
     *         .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
     *         .build()
     *     
     *     val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
     *     
     *     if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
     *         Log.w(TAG, "Google Fit permissions not granted")
     *         return
     *     }
     *     
     *     val response = Fitness.getHistoryClient(context, account)
     *         .readData(
     *             DataReadRequest.Builder()
     *                 .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
     *                 .bucketByTime(1, TimeUnit.DAYS)
     *                 .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
     *                 .build()
     *         )
     *         .await()
     *     
     *     response.buckets.forEach { bucket ->
     *         bucket.dataSets.forEach { dataSet ->
     *             dataSet.dataPoints.forEach { dataPoint ->
     *                 val steps = dataPoint.getValue(Field.FIELD_STEPS).asInt()
     *                 val start = dataPoint.getStartTime(TimeUnit.MILLISECONDS)
     *                 val end = dataPoint.getEndTime(TimeUnit.MILLISECONDS)
     *                 
     *                 // Upload to API
     *                 uploadMetric("steps", steps.toDouble(), start, end)
     *             }
     *         }
     *     }
     * }
     * ```
     */
    fun syncStepsFromGoogleFit() {
        Log.d(TAG, "Google Fit sync requires dependencies. See class documentation.")
    }
    
    /**
     * Đồng bộ nhịp tim từ Google Fit
     */
    fun syncHeartRateFromGoogleFit() {
        Log.d(TAG, "Google Fit sync requires dependencies. See class documentation.")
    }
    
    /**
     * Đồng bộ calories từ Google Fit
     */
    fun syncCaloriesFromGoogleFit() {
        Log.d(TAG, "Google Fit sync requires dependencies. See class documentation.")
    }
    
    /**
     * Đồng bộ giấc ngủ từ Google Fit
     */
    fun syncSleepFromGoogleFit() {
        Log.d(TAG, "Google Fit sync requires dependencies. See class documentation.")
    }
    
    /**
     * Upload metric lên API
     */
    private fun uploadMetric(
        metricType: String,
        value: Double,
        startTimeMillis: Long,
        endTimeMillis: Long
    ) {
        val token = tokenManager.getToken() ?: return
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        
        val startTime = dateFormat.format(Date(startTimeMillis))
        val endTime = dateFormat.format(Date(endTimeMillis))
        
        CoroutineScope(Dispatchers.IO).launch {
            val result = addHealthMetricsUseCase(
                token = token,
                metricType = metricType,
                value = value,
                startTime = startTime,
                endTime = endTime
            )
            
            result.onSuccess {
                Log.d(TAG, "Uploaded $metricType: $value")
            }.onFailure { error ->
                Log.e(TAG, "Failed to upload $metricType: ${error.message}")
            }
        }
    }
}
