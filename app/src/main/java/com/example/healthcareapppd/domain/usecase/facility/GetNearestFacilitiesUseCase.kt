package com.example.healthcareapppd.domain.usecase.facility

import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.Facility

class GetNearestFacilitiesUseCase {
    private val facilityApi = RetrofitClient.facilityApi
    
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        radius: Int? = null,
        limit: Int? = null,
        type: String? = null
    ): Result<List<Facility>> {
        return try {
            val response = facilityApi.getNearestFacilities(latitude, longitude, radius, limit, type)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception("Get nearest facilities failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
