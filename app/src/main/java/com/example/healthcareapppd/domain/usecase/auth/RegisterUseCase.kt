package com.example.healthcareapppd.domain.usecase.auth

import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*

class RegisterUseCase {
    private val authApi = RetrofitClient.authApi
    
    suspend operator fun invoke(
        email: String,
        password: String,
        role: String = "patient"
    ): Result<AuthResponse> {
        return try {
            val response = authApi.register(RegisterRequest(email, password, role))
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
