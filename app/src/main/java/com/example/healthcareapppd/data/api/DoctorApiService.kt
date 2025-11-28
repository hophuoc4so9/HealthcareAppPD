package com.example.healthcareapppd.data.api

import com.example.healthcareapppd.data.api.model.*
import retrofit2.Call
import retrofit2.http.*

interface DoctorApiService {
    
    @POST("api/doctors/profile")
    suspend fun createProfile(
        @Header("Authorization") token: String,
        @Body request: CreateDoctorProfileRequest
    ): ApiResponse<DoctorProfile>
    
    @GET("api/doctors/profile")
    suspend fun getMyProfile(
        @Header("Authorization") token: String
    ): ApiResponse<DoctorProfile>
    
    @PUT("api/doctors/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: CreateDoctorProfileRequest
    ): ApiResponse<DoctorProfile>
    
    @GET("api/doctors")
    suspend fun getAllDoctors(
        @Header("Authorization") token: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("status") status: String? = null
    ): DoctorsResponse
    
    @GET("api/doctors/search")
    suspend fun searchBySpecialization(
        @Query("q") query: String,
        @Query("limit") limit: Int? = null
    ): ApiResponse<List<DoctorProfile>>
    
    @GET("api/doctors/{id}/profile")
    suspend fun getDoctorById(
        @Path("id") userId: String
    ): ApiResponse<DoctorProfile>
    
    @PATCH("api/doctors/{id}/verification")
    suspend fun updateVerificationStatus(
        @Header("Authorization") token: String,
        @Path("id") userId: String,
        @Body request: UpdateVerificationStatusRequest
    ): ApiResponse<DoctorProfile>

    @GET("api/doctors/dashboard/stats")
    fun getDashboardStats(
    ): Call<DoctorDashboardStatsResponse>
}
