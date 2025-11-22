package com.example.healthcareapppd.domain.usecase.patient

import android.util.Log
import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*

class GetHealthMetricsUseCase {
    private val patientApi = RetrofitClient.patientApi
    
    suspend operator fun invoke(
        token: String,
        metricType: String,
        startDate: String? = null,
        endDate: String? = null
    ): Result<List<HealthMetric>> {
        return try {
            Log.d("GetHealthMetrics", "📡 Requesting metrics:")
            Log.d("GetHealthMetrics", "   - Type: $metricType")
            Log.d("GetHealthMetrics", "   - StartDate: $startDate")
            Log.d("GetHealthMetrics", "   - EndDate: $endDate")
            
            val response = patientApi.getMetrics("Bearer $token", metricType, startDate, endDate)
            
            Log.d("GetHealthMetrics", "📥 Response:")
            Log.d("GetHealthMetrics", "   - Success: ${response.success}")
            Log.d("GetHealthMetrics", "   - Count: ${response.data?.count}")
            Log.d("GetHealthMetrics", "   - Metrics: ${response.data?.metrics?.size}")
            
            if (response.success && response.data != null) {
                Result.success(response.data.metrics)
            } else {
                Result.failure(Exception(response.message ?: "Get metrics failed"))
            }
        } catch (e: Exception) {
            Log.e("GetHealthMetrics", "❌ Error: ${e.message}", e)
            Result.failure(e)
        }
    }
}
