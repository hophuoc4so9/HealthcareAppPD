package com.example.healthcareapppd.domain.usecase.patient

import android.util.Log
import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*

class AddHealthMetricsUseCase {
    private val patientApi = RetrofitClient.patientApi
    
    suspend operator fun invoke(
        token: String,
        metricType: String,
        value: Double,
        startTime: String,
        endTime: String
    ): Result<HealthMetric> {
        return try {
            Log.d("AddHealthMetrics", "📤 Posting metric:")
            Log.d("AddHealthMetrics", "   - Type: $metricType")
            Log.d("AddHealthMetrics", "   - Value: $value")
            Log.d("AddHealthMetrics", "   - Start: $startTime")
            Log.d("AddHealthMetrics", "   - End: $endTime")
            
            val request = AddMetricsRequest(metricType, value, startTime, endTime)
            val response = patientApi.addMetrics("Bearer $token", request)
            
            Log.d("AddHealthMetrics", "📥 Response:")
            Log.d("AddHealthMetrics", "   - Success: ${response.success}")
            Log.d("AddHealthMetrics", "   - Message: ${response.message}")
            Log.d("AddHealthMetrics", "   - Data: ${response.data}")
            
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Add metrics failed"))
            }
        } catch (e: Exception) {
            Log.e("AddHealthMetrics", "❌ Error: ${e.message}", e)
            Result.failure(e)
        }
    }
}
