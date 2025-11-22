package com.example.healthcareapppd.domain.usecase.patient

import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*

class GetMetricsSummaryUseCase {
    private val patientApi = RetrofitClient.patientApi
    
    suspend operator fun invoke(
        token: String,
        metricType: String,
        fromDate: String? = null,
        toDate: String? = null
    ): Result<MetricsSummary> {
        return try {
            val response = patientApi.getMetricsSummary("Bearer $token", metricType, fromDate, toDate)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Get metrics summary failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
