package com.example.healthcareapppd.domain.usecase.appointment

import android.content.Context
import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*
import com.example.healthcareapppd.utils.TokenManager

class BookAppointmentUseCase {
    private val appointmentApi = RetrofitClient.appointmentApi
    
    suspend operator fun invoke(
        context: Context,
        doctorUserId: String,
        availabilitySlotId: String,
        patientNotes: String? = null
    ): Result<Appointment> {
        return try {
            val token = TokenManager.getToken(context) 
                ?: return Result.failure(Exception("Không tìm thấy token. Vui lòng đăng nhập lại."))
            
            val request = BookAppointmentRequest(doctorUserId, availabilitySlotId, patientNotes)
            val response = appointmentApi.bookAppointment("Bearer $token", request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Đặt lịch hẹn thất bại"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
