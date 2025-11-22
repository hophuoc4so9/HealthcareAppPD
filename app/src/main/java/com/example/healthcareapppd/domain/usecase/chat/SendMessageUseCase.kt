package com.example.healthcareapppd.domain.usecase.chat

import android.content.Context
import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*
import com.example.healthcareapppd.utils.TokenManager

class SendMessageUseCase {
    private val chatApi = RetrofitClient.chatApi
    
    suspend operator fun invoke(
        context: Context,
        conversationId: String,
        messageContent: String
    ): Result<ChatMessage> {
        return try {
            val token = TokenManager.getToken(context)
                ?: return Result.failure(Exception("Token not found"))
            
            val request = SendMessageRequest(messageContent)
            val response = chatApi.sendMessage("Bearer $token", conversationId, request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Send message failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
