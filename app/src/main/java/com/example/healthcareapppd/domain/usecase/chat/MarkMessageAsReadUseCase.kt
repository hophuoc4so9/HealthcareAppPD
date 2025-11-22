package com.example.healthcareapppd.domain.usecase.chat

import com.example.healthcareapppd.data.api.RetrofitClient

class MarkMessageAsReadUseCase {
    private val chatApi = RetrofitClient.chatApi
    
    suspend operator fun invoke(token: String, messageId: Long): Result<Unit> {
        return try {
            val response = chatApi.markAsRead("Bearer $token", messageId)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Mark as read failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
