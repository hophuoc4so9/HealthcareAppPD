package com.example.healthcareapppd.domain.usecase.appointment

import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*

class GetAvailabilitySlotsUseCase {
    private val appointmentApi = RetrofitClient.appointmentApi
    
    suspend operator fun invoke(
        token: String,
        fromDate: String? = null,
        toDate: String? = null,
        isBooked: Boolean? = null
    ): Result<List<AvailabilitySlot>> {
        return try {
            val response = appointmentApi.getMyAvailability("Bearer $token", fromDate, toDate, isBooked)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Get availability failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
