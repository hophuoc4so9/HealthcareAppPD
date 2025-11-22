package com.example.healthcareapppd.data.api

import com.example.healthcareapppd.data.api.model.*
import com.google.gson.annotations.SerializedName
import retrofit2.http.*
import com.google.android.gms.maps.model.LatLng

interface FacilitiesApiService {
    
    @GET("api/facilities")
    suspend fun getAllFacilities(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): ApiResponse<List<Facility>>
    
    @GET("api/facilities/search")
    suspend fun searchFacilities(
        @Query("name") name: String? = null,
        @Query("type") type: String? = null,
        @Query("city") city: String? = null,
        @Query("limit") limit: Int? = null
    ): ApiResponse<List<Facility>>
    
    @GET("api/facilities/nearest")
    suspend fun getNearestFacilities(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("type") type: String? = null
    ): ApiResponse<List<Facility>>
    
    @GET("api/facilities/stats")
    suspend fun getFacilitiesStats(
        @Query("city") city: String? = null
    ): FacilityStatsResponse
    
    @GET("api/facilities/type/{type}")
    suspend fun getFacilitiesByType(
        @Path("type") type: String,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("city") city: String? = null
    ): ApiResponse<List<Facility>>
    
    @POST("api/facilities/in-area")
    suspend fun getFacilitiesInArea(
        @Body request: FacilityInAreaRequest
    ): ApiResponse<List<Facility>>
    
    @GET("api/facilities/{id}")
    suspend fun getFacilityById(
        @Path("id") facilityId: Int
    ): FacilityDetailResponse
}

private val wktPointRegex = """POINT\(\s*(-?\d+\.?\d*)\s+(-?\d+\.?\d*)\s*\)""".toRegex()

fun Facility.getLatLng(): LatLng? {
    val matchResult = wktPointRegex.find(this.geom)
    return if (matchResult != null) {
        try {
            val (lng, lat) = matchResult.destructured
            // WKT là (kinh độ, vĩ độ), hàm tạo LatLng là (vĩ độ, kinh độ)
            LatLng(lat.toDouble(), lng.toDouble())
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            null
        }
    } else {
        null
    }
}

// Helper function để lấy khoảng cách từ API hoặc tính toán
fun Facility.getDistance(): Int? = distanceMeters