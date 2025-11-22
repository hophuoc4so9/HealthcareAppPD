package com.example.healthcareapppd.domain.usecase.reminder

import android.content.Context
import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*
import com.example.healthcareapppd.utils.TokenManager

class ToggleReminderUseCase {
    private val reminderApi = RetrofitClient.reminderApi
    
    suspend operator fun invoke(
        context: Context,
        reminderId: String,
        isActive: Boolean
    ): Result<Reminder> {
        return try {
            val token = TokenManager.getToken(context)
                ?: return Result.failure(Exception("Token not found"))
            
            val request = ToggleActiveRequest(isActive)
            val response = reminderApi.toggleActive("Bearer $token", reminderId, request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Toggle reminder failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
