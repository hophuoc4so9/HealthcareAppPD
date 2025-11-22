package com.example.healthcareapppd.domain.usecase.auth

import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*

class VerifyTokenUseCase {
    private val authApi = RetrofitClient.authApi
    
    suspend operator fun invoke(token: String): Result<User> {
        return try {
            val response = authApi.verifyToken(VerifyTokenRequest(token))
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Token verification failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
