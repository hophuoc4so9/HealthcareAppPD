package com.example.healthcareapppd.domain.usecase.chat

import android.content.Context
import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*
import com.example.healthcareapppd.utils.TokenManager

class GetMessagesUseCase {
    private val chatApi = RetrofitClient.chatApi
    
    suspend operator fun invoke(
        context: Context,
        conversationId: String,
        limit: Int? = null
    ): Result<List<ChatMessage>> {
        return try {
            val token = TokenManager.getToken(context)
                ?: return Result.failure(Exception("Token not found"))
            
            val response = chatApi.getMessages("Bearer $token", conversationId, limit)
            if (response.success && response.data != null) {
                Result.success(response.data.messages)
            } else {
                Result.failure(Exception(response.message ?: "Get messages failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
