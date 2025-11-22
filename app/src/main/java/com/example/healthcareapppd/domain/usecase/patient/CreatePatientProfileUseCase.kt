package com.example.healthcareapppd.domain.usecase.patient

import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*

class CreatePatientProfileUseCase {
    private val patientApi = RetrofitClient.patientApi
    
    suspend operator fun invoke(
        token: String,
        fullName: String,
        dateOfBirth: String,
        sex: String,
        phoneNumber: String? = null,
        address: String? = null,
        emergencyContactName: String? = null,
        emergencyContactPhone: String? = null
    ): Result<PatientProfile> {
        return try {
            val request = CreatePatientProfileRequest(
                fullName, dateOfBirth, sex, phoneNumber, address, 
                emergencyContactName, emergencyContactPhone
            )
            val response = patientApi.createProfile("Bearer $token", request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Create profile failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
