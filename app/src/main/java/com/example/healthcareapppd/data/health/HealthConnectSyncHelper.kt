package com.example.healthcareapppd.data.health

import android.content.Context
import android.util.Log
import com.example.healthcareapppd.utils.TokenManager
import com.example.healthcareapppd.domain.usecase.patient.AddHealthMetricsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper để sync dữ liệu từ Health Connect lên API server
 */
class HealthConnectSyncHelper(
    private val context: Context,
    private val tokenManager: TokenManager,
    private val addHealthMetricsUseCase: AddHealthMetricsUseCase,
    private val healthConnectManager: HealthConnectManager
) {
    
    companion object {
        private const val TAG = "HealthConnectSync"
    }
    
    /**
     * Sync dữ liệu hôm nay lên server
     */
    suspend fun syncTodayData(onProgress: (String) -> Unit = {}): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken() 
                    ?: return@withContext Result.failure(Exception("Chưa đăng nhập"))
                
                onProgress("📱 Đang đọc dữ liệu từ Health Connect...")
                
                // Đọc dữ liệu hôm nay
                val dataResult = healthConnectManager.readTodayData()
                
                if (dataResult.isFailure) {
                    return@withContext Result.failure(
                        dataResult.exceptionOrNull() ?: Exception("Không đọc được dữ liệu")
                    )
                }
                
                val data = dataResult.getOrNull() 
                    ?: return@withContext Result.failure(Exception("Dữ liệu rỗng"))
                
                Log.d(TAG, "📊 Dữ liệu từ Health Connect:")
                Log.d(TAG, "   - Steps: ${data.steps}")
                Log.d(TAG, "   - Calories: ${data.calories}")
                Log.d(TAG, "   - Distance: ${data.distanceMeters}m")
                Log.d(TAG, "   - Sleep: ${data.sleepMinutes} minutes")
                
                // Tạo time range cho hôm nay - format ISO 8601 với UTC timezone
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                
                // Start time: 00:00 hôm nay UTC
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startTime = dateFormat.format(calendar.time)
                
                // End time: hiện tại UTC
                val endTime = dateFormat.format(Date())
                
                Log.d(TAG, "🕒 Time range:")
                Log.d(TAG, "   - Start: $startTime")
                Log.d(TAG, "   - End: $endTime")
                
                // Upload từng metric
                var successCount = 0
                var failCount = 0
                
                // 1. Bước chân
                if (data.steps > 0) {
                    onProgress("👣 Đang upload ${data.steps} bước...")
                    val result = addHealthMetricsUseCase(
                        token = token,
                        metricType = "steps",
                        value = data.steps.toDouble(),
                        startTime = startTime,
                        endTime = endTime
                    )
                    if (result.isSuccess) {
                        successCount++
                        Log.d(TAG, "✅ Uploaded steps: ${data.steps}")
                    } else {
                        failCount++
                        Log.e(TAG, "❌ Failed to upload steps: ${result.exceptionOrNull()?.message}")
                    }
                }
                
                // 2. Calories
                if (data.calories > 0) {
                    onProgress("🔥 Đang upload ${data.calories.toInt()} kcal...")
                    val result = addHealthMetricsUseCase(
                        token = token,
                        metricType = "active_calories",
                        value = data.calories,
                        startTime = startTime,
                        endTime = endTime
                    )
                    if (result.isSuccess) {
                        successCount++
                        Log.d(TAG, "✅ Uploaded calories: ${data.calories}")
                    } else {
                        failCount++
                        Log.e(TAG, "❌ Failed to upload calories")
                    }
                }
                
                // 3. Quãng đường
                if (data.distanceMeters > 0) {
                    onProgress("🚶 Đang upload ${data.distanceMeters.toInt()}m...")
                    val result = addHealthMetricsUseCase(
                        token = token,
                        metricType = "distance_meters",
                        value = data.distanceMeters,
                        startTime = startTime,
                        endTime = endTime
                    )
                    if (result.isSuccess) {
                        successCount++
                        Log.d(TAG, "✅ Uploaded distance: ${data.distanceMeters}m")
                    } else {
                        failCount++
                        Log.e(TAG, "❌ Failed to upload distance")
                    }
                }
                
                // 4. Giấc ngủ
                if (data.sleepMinutes > 0) {
                    onProgress("😴 Đang upload ${data.sleepMinutes} phút ngủ...")
                    
                    // Sleep time: từ 20:00 ngày hôm trước (khớp với readSleep trong HealthConnectManager)
                    val sleepCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    sleepCalendar.add(Calendar.DAY_OF_MONTH, -1)
                    sleepCalendar.set(Calendar.HOUR_OF_DAY, 20)
                    sleepCalendar.set(Calendar.MINUTE, 0)
                    sleepCalendar.set(Calendar.SECOND, 0)
                    sleepCalendar.set(Calendar.MILLISECOND, 0)
                    val sleepStart = dateFormat.format(sleepCalendar.time)
                    
                    // End: start + sleepMinutes
                    sleepCalendar.add(Calendar.MINUTE, data.sleepMinutes)
                    val sleepEnd = dateFormat.format(sleepCalendar.time)
                    
                    Log.d(TAG, "💤 Sleep time range: $sleepStart -> $sleepEnd (${data.sleepMinutes} min)")
                    
                    val result = addHealthMetricsUseCase(
                        token = token,
                        metricType = "sleep_duration_minutes",
                        value = data.sleepMinutes.toDouble(),
                        startTime = sleepStart,
                        endTime = sleepEnd
                    )
                    if (result.isSuccess) {
                        successCount++
                        Log.d(TAG, "✅ Uploaded sleep: ${data.sleepMinutes} minutes")
                    } else {
                        failCount++
                        Log.e(TAG, "❌ Failed to upload sleep: ${result.exceptionOrNull()?.message}")
                    }
                }
                
                val totalCount = successCount + failCount
                val message = "✅ Đã upload $successCount/$totalCount chỉ số"
                onProgress(message)
                
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing data", e)
                Result.failure(e)
            }
        }
    }
}
