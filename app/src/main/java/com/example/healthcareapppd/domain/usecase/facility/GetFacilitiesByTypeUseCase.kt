package com.example.healthcareapppd.domain.usecase.facility

import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.Facility

class GetFacilitiesByTypeUseCase {
    private val facilityApi = RetrofitClient.facilityApi
    
    suspend operator fun invoke(
        type: String,
        page: Int? = null,
        limit: Int? = null,
        city: String? = null
    ): Result<List<Facility>> {
        return try {
            val response = facilityApi.getFacilitiesByType(type, page, limit, city)
            if (response.success && response.data != null) {
                Result.success(response.data ?: emptyList())
            } else {
                Result.failure(Exception("Get facilities by type failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
