package com.example.healthcareapppd.domain.usecase.reminder

import android.content.Context
import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.utils.TokenManager

class DeleteReminderUseCase {
    private val reminderApi = RetrofitClient.reminderApi
    
    suspend operator fun invoke(context: Context, reminderId: String): Result<Unit> {
        return try {
            val token = TokenManager.getToken(context)
                ?: return Result.failure(Exception("Token not found"))
            
            val response = reminderApi.deleteReminder("Bearer $token", reminderId)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Delete reminder failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
