package com.example.healthcareapppd.domain.usecase.reminder

import android.content.Context
import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*
import com.example.healthcareapppd.utils.TokenManager

class GetMyRemindersUseCase {
    private val reminderApi = RetrofitClient.reminderApi
    
    suspend operator fun invoke(
        context: Context
    ): Result<List<Reminder>> {
        return try {
            val token = TokenManager.getToken(context) 
                ?: return Result.failure(Exception("Không tìm thấy token. Vui lòng đăng nhập lại."))
            
            val response = reminderApi.getMyReminders("Bearer $token")
            if (response.success && response.data?.reminders != null) {
                Result.success(response.data.reminders)
            } else {
                Result.failure(Exception(response.data?.toString() ?: "Không thể tải danh sách nhắc nhở"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
