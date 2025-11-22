package com.example.healthcareapppd.domain.usecase.facility

import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.Facility

class SearchFacilitiesUseCase {
    private val facilityApi = RetrofitClient.facilityApi
    
    suspend operator fun invoke(
        name: String? = null,
        type: String? = null,
        city: String? = null,
        limit: Int? = null
    ): Result<List<Facility>> {
        return try {
            val response = facilityApi.searchFacilities(name, type, city, limit)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception("Search facilities failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
