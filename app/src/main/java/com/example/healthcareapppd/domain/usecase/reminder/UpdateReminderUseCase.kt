package com.example.healthcareapppd.domain.usecase.reminder

import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*

class UpdateReminderUseCase {
    private val reminderApi = RetrofitClient.reminderApi
    
    suspend operator fun invoke(
        token: String,
        reminderId: String,
        title: String? = null,
        description: String? = null,
        reminderType: String? = null,
        cronExpression: String? = null,
        oneTimeAt: String? = null,
        timezoneName: String? = null
    ): Result<Reminder> {
        return try {
            val request = UpdateReminderRequest(
                title = title,
                description = description,
                reminderType = reminderType,
                cronExpression = cronExpression,
                oneTimeAt = oneTimeAt,
                timezoneName = timezoneName
            )
            val response = reminderApi.updateReminder("Bearer $token", reminderId, request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Update reminder failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
