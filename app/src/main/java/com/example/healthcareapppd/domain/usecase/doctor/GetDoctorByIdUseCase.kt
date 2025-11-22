package com.example.healthcareapppd.domain.usecase.doctor

import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*

class GetDoctorByIdUseCase {
    private val doctorApi = RetrofitClient.doctorApi
    
    suspend operator fun invoke(doctorId: String): Result<DoctorProfile> {
        return try {
            val response = doctorApi.getDoctorById(doctorId)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Get doctor failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
