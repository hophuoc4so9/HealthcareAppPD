package com.example.healthcareapppd.domain.usecase.doctor

import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*

class SearchDoctorsBySpecializationUseCase {
    private val doctorApi = RetrofitClient.doctorApi
    
    suspend operator fun invoke(
        query: String,
        limit: Int? = null
    ): Result<List<DoctorProfile>> {
        return try {
            val response = doctorApi.searchBySpecialization(query, limit)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Search doctors failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
