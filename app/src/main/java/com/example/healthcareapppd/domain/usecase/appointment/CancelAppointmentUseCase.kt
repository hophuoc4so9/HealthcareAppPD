package com.example.healthcareapppd.domain.usecase.appointment

import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*

class CancelAppointmentUseCase {
    private val appointmentApi = RetrofitClient.appointmentApi
    
    suspend operator fun invoke(token: String, appointmentId: String): Result<Appointment> {
        return try {
            val response = appointmentApi.cancelAppointment("Bearer $token", appointmentId)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Cancel appointment failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
