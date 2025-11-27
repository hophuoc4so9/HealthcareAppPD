package com.example.healthcareapppd.data.health

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit


class HealthConnectManager(private val context: Context) {
    
    companion object {
        private const val TAG = "HealthConnect"
        
        // Health Connect permissions
        val PERMISSIONS = setOf(
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(DistanceRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class)
        )
    }
    
    private val healthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }
    
    /**
     * Kiểm tra Health Connect có khả dụng không
     */
    fun isAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }
    
    /**
     * Kiểm tra đã có permissions chưa
     */
    suspend fun hasAllPermissions(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val granted = healthConnectClient.permissionController.getGrantedPermissions()
                PERMISSIONS.all { it in granted }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking permissions", e)
                false
            }
        }
    }
    
    /**
     * Contract để request permissions
     */
    class PermissionsContract : ActivityResultContract<Set<String>, Set<String>>() {
        override fun createIntent(context: Context, input: Set<String>): Intent {
            return PermissionController.createRequestPermissionResultContract()
                .createIntent(context, input)
        }
        
        override fun parseResult(resultCode: Int, intent: Intent?): Set<String> {
            return PermissionController.createRequestPermissionResultContract()
                .parseResult(resultCode, intent)
        }
    }
    
    /**
     * Mở Health Connect app để grant permissions
     */
    fun openHealthConnectSettings() {
        val intent = Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
    
    /**
     * Data class chứa dữ liệu hôm nay
     */
    data class TodayHealthData(
        val steps: Int = 0,
        val calories: Double = 0.0,
        val distanceMeters: Double = 0.0,
        val sleepMinutes: Int = 0
    )
    
    /**
     * Đọc tất cả dữ liệu hôm nay
     */
    suspend fun readTodayData(): Result<TodayHealthData> {
        return withContext(Dispatchers.IO) {
            try {
                val (startTime, endTime) = getTodayTimeRange()
                
                val steps = readSteps(startTime, endTime)
                val calories = readCalories(startTime, endTime)
                val distance = readDistance(startTime, endTime)
                val sleep = readSleep(startTime, endTime)
                
                val data = TodayHealthData(
                    steps = steps,
                    calories = calories,
                    distanceMeters = distance,
                    sleepMinutes = sleep
                )
                
                Log.d(TAG, "Today data: $data")
                Result.success(data)
            } catch (e: Exception) {
                Log.e(TAG, "Error reading today data", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Đọc số bước chân
     */
    private suspend fun readSteps(startTime: Instant, endTime: Instant): Int {
        return try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            
            val totalSteps = response.records.sumOf { it.count }
            Log.d(TAG, "Steps: $totalSteps")
            totalSteps.toInt()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading steps", e)
            0
        }
    }
    
    /**
     * Đọc calories tiêu hao (chỉ active calories, không bao gồm BMR)
     */
    private suspend fun readCalories(startTime: Instant, endTime: Instant): Double {
        return try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = ActiveCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            
            val totalCalories = response.records.sumOf { 
                it.energy.inKilocalories 
            }
            Log.d(TAG, "Active Calories: $totalCalories kcal (from ${response.records.size} records)")
            totalCalories
        } catch (e: Exception) {
            Log.e(TAG, "Error reading active calories", e)
            0.0
        }
    }
    
    /**
     * Đọc quãng đường
     */
    private suspend fun readDistance(startTime: Instant, endTime: Instant): Double {
        return try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = DistanceRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            
            val totalDistance = response.records.sumOf { 
                it.distance.inMeters 
            }
            Log.d(TAG, "Distance: $totalDistance meters")
            totalDistance
        } catch (e: Exception) {
            Log.e(TAG, "Error reading distance", e)
            0.0
        }
    }
    
    /**
     * Đọc giấc ngủ (từ đêm qua đến sáng nay)
     */
    private suspend fun readSleep(startTime: Instant, endTime: Instant): Int {
        return try {
            // Lấy sleep từ 20:00 ngày hôm trước đến 12:00 hôm nay
            val sleepStart = startTime.minus(4, ChronoUnit.HOURS)
            val sleepEnd = endTime
            
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(sleepStart, sleepEnd)
                )
            )
            
            val totalMinutes = response.records.sumOf { session ->
                val duration = java.time.Duration.between(session.startTime, session.endTime)
                duration.toMinutes()
            }
            
            Log.d(TAG, "Sleep: $totalMinutes minutes")
            totalMinutes.toInt()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading sleep", e)
            0
        }
    }
    
    /**
     * Đọc dữ liệu trong khoảng thời gian
     */
    suspend fun readDataInRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Result<List<DailyHealthData>> {
        return withContext(Dispatchers.IO) {
            try {
                val dailyDataList = mutableListOf<DailyHealthData>()
                var currentDate = startDate
                
                while (!currentDate.isAfter(endDate)) {
                    val dayStart = currentDate.atZone(ZoneId.systemDefault()).toInstant()
                    val dayEnd = currentDate.plusDays(1).atZone(ZoneId.systemDefault()).toInstant()
                    
                    val steps = readSteps(dayStart, dayEnd)
                    val calories = readCalories(dayStart, dayEnd)
                    val distance = readDistance(dayStart, dayEnd)
                    val sleep = readSleep(dayStart, dayEnd)
                    
                    dailyDataList.add(
                        DailyHealthData(
                            date = currentDate,
                            steps = steps,
                            calories = calories,
                            distanceMeters = distance,
                            sleepMinutes = sleep
                        )
                    )
                    
                    currentDate = currentDate.plusDays(1)
                }
                
                Result.success(dailyDataList)
            } catch (e: Exception) {
                Log.e(TAG, "Error reading range data", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Data class cho dữ liệu hàng ngày
     */
    data class DailyHealthData(
        val date: LocalDateTime,
        val steps: Int,
        val calories: Double,
        val distanceMeters: Double,
        val sleepMinutes: Int
    )
    
    /**
     * Lấy time range cho hôm nay (00:00 - 23:59)
     */
    private fun getTodayTimeRange(): Pair<Instant, Instant> {
        val now = LocalDateTime.now()
        val startOfDay = now.toLocalDate().atStartOfDay()
        val endOfDay = now
        
        val startTime = startOfDay.atZone(ZoneId.systemDefault()).toInstant()
        val endTime = endOfDay.atZone(ZoneId.systemDefault()).toInstant()
        
        return Pair(startTime, endTime)
    }
    
    /**
     * Kiểm tra Health Connect app đã cài chưa
     */
    fun isHealthConnectInstalled(): Boolean {
        val availabilityStatus = HealthConnectClient.getSdkStatus(context)
        return availabilityStatus != HealthConnectClient.SDK_UNAVAILABLE
    }
    
    /**
     * Mở Play Store để cài Health Connect
     */
    fun installHealthConnect() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
            setPackage("com.android.vending")
        }
        context.startActivity(intent)
    }
}
