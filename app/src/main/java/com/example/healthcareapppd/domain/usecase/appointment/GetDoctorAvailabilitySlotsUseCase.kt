package com.example.healthcareapppd.domain.usecase.appointment

import android.content.Context
import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*
import com.example.healthcareapppd.utils.TokenManager

class GetDoctorAvailabilitySlotsUseCase {
    private val appointmentApi = RetrofitClient.appointmentApi
    
    suspend operator fun invoke(
        context: Context,
        doctorUserId: String,
        date: String
    ): Result<List<AvailabilitySlot>> {
        return try {
            val token = TokenManager.getToken(context) 
                ?: return Result.failure(Exception("Không tìm thấy token. Vui lòng đăng nhập lại."))
            
            val response = appointmentApi.getDoctorAvailableSlots(
                token = "Bearer $token",
                doctorUserId = doctorUserId,
                date = date
            )
            
            if (response.success && response.data != null) {
                Result.success(response.data.slots ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "Không thể tải lịch trống"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
