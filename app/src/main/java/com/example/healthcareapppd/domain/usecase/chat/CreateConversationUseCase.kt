package com.example.healthcareapppd.domain.usecase.chat

import android.content.Context
import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*
import com.example.healthcareapppd.utils.TokenManager

class CreateConversationUseCase {
    private val chatApi = RetrofitClient.chatApi
    
    suspend operator fun invoke(context: Context, targetUserId: String): Result<Conversation> {
        return try {
            val token = TokenManager.getToken(context)
                ?: return Result.failure(Exception("Token not found"))
            
            val request = CreateConversationRequest(targetUserId)
            val response = chatApi.createConversation("Bearer $token", request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Create conversation failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
