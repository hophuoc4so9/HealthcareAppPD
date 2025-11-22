package com.example.healthcareapppd.domain.usecase.reminder

import android.content.Context
import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*
import com.example.healthcareapppd.utils.TokenManager

class CreateReminderUseCase {
    private val reminderApi = RetrofitClient.reminderApi
    
    suspend operator fun invoke(
        context: Context,
        title: String,
        description: String?,
        reminderType: String,
        cronExpression: String? = null,
        oneTimeAt: String? = null,
        timezoneName: String = "Asia/Ho_Chi_Minh"
    ): Result<Reminder> {
        return try {
            val token = TokenManager.getToken(context)
                ?: return Result.failure(Exception("Token not found"))
            
            val request = CreateReminderRequest(
                title = title,
                description = description,
                reminderType = reminderType,
                cronExpression = cronExpression,
                oneTimeAt = oneTimeAt,
                timezoneName = timezoneName
            )
            val response = reminderApi.createReminder("Bearer $token", request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Create reminder failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
