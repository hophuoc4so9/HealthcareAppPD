package com.example.healthcareapppd.domain.repository

import com.example.healthcareapppd.data.api.ApiResponse
import com.example.healthcareapppd.data.api.Facility
import com.example.healthcareapppd.data.api.FacilitiesApiService

class FacilitiesRepository(
    private val apiService: FacilitiesApiService
) {
    // Hàm này gọi API và trả về một đối tượng Result để xử lý thành công/thất bại
    suspend fun getNearestFacilities(params: Map<String, String>): Result<List<Facility>> {
        return try {
            val response: ApiResponse = apiService.getNearestFacilities(params)
            if (response.success) {
                Result.success(response.data)
            } else {
                Result.failure(Exception("API request was not successful"))
            }
        } catch (e: Exception) {
            // Bắt các lỗi mạng như HttpException, IOException, v.v.
            Result.failure(e)
        }
    }
}