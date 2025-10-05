package com.example.healthcareapppd.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.QueryMap
import com.google.android.gms.maps.model.LatLng

interface FacilitiesApiService {
    @GET("api/facilities/nearest")
    suspend fun getNearestFacilities(
        @QueryMap params: Map<String, String>
    ): ApiResponse
}

data class ApiResponse(
    val success: Boolean,
    val data: List<Facility>
)

data class Facility(
    val id: Int,
    val name: String?,
    @SerializedName("amenity") val type: String,
    @SerializedName("addr_full") val address: String?,
    val geom: String,
    @SerializedName("distance_meters") val distanceMeters: Int? = null // Thêm field từ API
)

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