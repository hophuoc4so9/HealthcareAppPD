package com.example.healthcareapppd.domain.usecase.patient

import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*

class GetVitalsHistoryUseCase {
    private val patientApi = RetrofitClient.patientApi
    
    suspend operator fun invoke(
        token: String,
        limit: Int? = null
    ): Result<List<Vitals>> {
        return try {
            val response = patientApi.getVitalsHistory("Bearer $token", limit)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Get vitals failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
