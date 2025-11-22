package com.example.healthcareapppd.data.api

import com.example.healthcareapppd.data.api.model.*
import retrofit2.http.*

interface PatientApiService {
    
    // ==================== PROFILE ====================
    @POST("api/patients/profile")
    suspend fun createProfile(
        @Header("Authorization") token: String,
        @Body request: CreatePatientProfileRequest
    ): ApiResponse<PatientProfile>
    
    @GET("api/patients/profile")
    suspend fun getMyProfile(
        @Header("Authorization") token: String
    ): ApiResponse<PatientProfile>
    
    @PUT("api/patients/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdatePatientProfileRequest
    ): ApiResponse<PatientProfile>
    
    @GET("api/patients/{id}/profile")
    suspend fun getProfileById(
        @Header("Authorization") token: String,
        @Path("id") userId: String
    ): ApiResponse<PatientProfile>
    
    // ==================== VITALS ====================
    @POST("api/patients/vitals")
    suspend fun addVitals(
        @Header("Authorization") token: String,
        @Body request: AddVitalsRequest
    ): ApiResponse<Vitals>
    
    @GET("api/patients/vitals")
    suspend fun getVitalsHistory(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int? = null
    ): ApiResponse<List<Vitals>>
    
    @GET("api/patients/vitals/latest")
    suspend fun getLatestVitals(
        @Header("Authorization") token: String
    ): ApiResponse<Vitals>
    
    @DELETE("api/patients/vitals/{id}")
    suspend fun deleteVitals(
        @Header("Authorization") token: String,
        @Path("id") vitalsId: Long
    ): ApiResponse<Any>
    
    // ==================== METRICS ====================
    @POST("api/patients/metrics")
    suspend fun addMetrics(
        @Header("Authorization") token: String,
        @Body request: AddMetricsRequest
    ): ApiResponse<HealthMetric>
    
    @GET("api/patients/metrics")
    suspend fun getMetrics(
        @Header("Authorization") token: String,
        @Query("metricType") metricType: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): ApiResponse<MetricsData>
    
    @GET("api/patients/metrics/summary")
    suspend fun getMetricsSummary(
        @Header("Authorization") token: String,
        @Query("metricType") metricType: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): ApiResponse<MetricsSummary>
    
    @DELETE("api/patients/metrics/{id}")
    suspend fun deleteMetrics(
        @Header("Authorization") token: String,
        @Path("id") metricId: Long
    ): ApiResponse<Any>
}
