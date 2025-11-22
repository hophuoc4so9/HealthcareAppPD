package com.example.healthcareapppd.domain.usecase.auth

import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*

class ChangePasswordUseCase {
    private val authApi = RetrofitClient.authApi
    
    suspend operator fun invoke(
        token: String,
        currentPassword: String,
        newPassword: String
    ): Result<Boolean> {
        return try {
            val response = authApi.changePassword(
                "Bearer $token",
                ChangePasswordRequest(currentPassword, newPassword)
            )
            if (response.success && response.data != null) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message ?: "Change password failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
