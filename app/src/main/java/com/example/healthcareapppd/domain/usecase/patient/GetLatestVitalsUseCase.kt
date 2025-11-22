package com.example.healthcareapppd.domain.usecase.patient

import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*

class GetLatestVitalsUseCase {
    private val patientApi = RetrofitClient.patientApi
    
    suspend operator fun invoke(token: String): Result<Vitals> {
        return try {
            val response = patientApi.getLatestVitals("Bearer $token")
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Get latest vitals failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
