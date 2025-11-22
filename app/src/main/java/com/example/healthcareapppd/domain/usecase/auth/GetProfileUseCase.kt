package com.example.healthcareapppd.domain.usecase.auth

import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*

class GetProfileUseCase {
    private val authApi = RetrofitClient.authApi
    
    suspend operator fun invoke(token: String): Result<User> {
        return try {
            val response = authApi.getProfile("Bearer $token")
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Get profile failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
