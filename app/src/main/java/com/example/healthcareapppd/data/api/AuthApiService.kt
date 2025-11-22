package com.example.healthcareapppd.data.api

import com.example.healthcareapppd.data.api.model.*
import retrofit2.http.*

interface AuthApiService {
    
    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): ApiResponse<AuthResponse>
    
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): ApiResponse<AuthResponse>
    
    @POST("api/auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): ApiResponse<Any>
    
    @GET("api/auth/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): ApiResponse<User>
    
    @POST("api/auth/verify-token")
    suspend fun verifyToken(
        @Body request: VerifyTokenRequest
    ): ApiResponse<User>
}
