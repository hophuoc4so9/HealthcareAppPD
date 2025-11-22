package com.example.healthcareapppd.domain.usecase.doctor

import android.content.Context
import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*
import com.example.healthcareapppd.utils.TokenManager

class GetAllDoctorsUseCase {
    private val doctorApi = RetrofitClient.doctorApi
    
    suspend operator fun invoke(
        context: Context? = null,
        page: Int? = null,
        limit: Int? = null,
        status: String? = null
    ): Result<List<DoctorProfile>> {
        return try {
            val token = context?.let { TokenManager.getToken(it) }
            val authHeader = token?.let { "Bearer $it" }
            val response = doctorApi.getAllDoctors(authHeader, page, limit, status)
            if (response.success && response.data?.doctors != null) {
                Result.success(response.data.doctors)
            } else {
                Result.failure(Exception("Get doctors failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
