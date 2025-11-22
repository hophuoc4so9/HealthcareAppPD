package com.example.healthcareapppd.domain.repository

import com.example.healthcareapppd.data.api.FacilitiesApiService
import com.example.healthcareapppd.data.api.model.ApiResponse
import com.example.healthcareapppd.data.api.model.Facility

class FacilitiesRepository(
    private val apiService: FacilitiesApiService
) {
    // Hàm này gọi API và trả về một đối tượng Result để xử lý thành công/thất bại
    suspend fun getNearestFacilities(
        lat: Double,
        lng: Double,
        radius: Int? = null,
        limit: Int? = null,
        type: String? = null
    ): Result<List<Facility>> {
        return try {
            val response: ApiResponse<List<Facility>> = apiService.getNearestFacilities(
                lat = lat,
                lng = lng,
                radius = radius,
                limit = limit,
                type = type
            )
            if (response.success && response.data != null) {
                Result.success(response.data ?: emptyList())
            } else {
                Result.failure(Exception(response.error ?: "API request was not successful"))
            }
        } catch (e: Exception) {
            // Bắt các lỗi mạng như HttpException, IOException, v.v.
            Result.failure(e)
        }
    }
}