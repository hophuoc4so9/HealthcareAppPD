package com.example.healthcareapppd.domain.usecase.facility

import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.Facility

class GetFacilityByIdUseCase {
    private val facilityApi = RetrofitClient.facilityApi
    
    suspend operator fun invoke(facilityId: Int): Result<Facility> {
        return try {
            val response = facilityApi.getFacilityById(facilityId)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception("Get facility failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
