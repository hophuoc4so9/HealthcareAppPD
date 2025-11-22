package com.example.healthcareapppd.domain.usecase.patient

import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*

class GetPatientProfileUseCase {
    private val patientApi = RetrofitClient.patientApi
    
    suspend operator fun invoke(token: String): Result<PatientProfile> {
        return try {
            val response = patientApi.getMyProfile("Bearer $token")
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Get profile failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
