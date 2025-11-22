package com.example.healthcareapppd.domain.usecase.appointment

import android.content.Context
import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*
import com.example.healthcareapppd.utils.TokenManager

class GetMyAppointmentsUseCase {
    private val appointmentApi = RetrofitClient.appointmentApi
    
    suspend operator fun invoke(
        context: Context,
        status: String? = null,
        fromDate: String? = null,
        toDate: String? = null
    ): Result<List<Appointment>> {
        return try {
            val token = TokenManager.getToken(context)
                ?: return Result.failure(Exception("Token not found"))
            
            val response = appointmentApi.getMyAppointments("Bearer $token", status, fromDate, toDate)
            if (response.success && response.data != null) {
                Result.success(response.data.appointments)
            } else {
                Result.failure(Exception(response.message ?: "Get appointments failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
