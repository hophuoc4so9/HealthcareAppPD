package com.example.healthcareapppd.domain.usecase.chat

import android.content.Context
import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*
import com.example.healthcareapppd.utils.TokenManager

class GetConversationsUseCase {
    private val chatApi = RetrofitClient.chatApi
    
    suspend operator fun invoke(context: Context): Result<List<Conversation>> {
        return try {
            val token = TokenManager.getToken(context)
                ?: return Result.failure(Exception("Token not found"))
            
            val response = chatApi.getMyConversations("Bearer $token")
            if (response.success && response.data != null) {
                Result.success(response.data.conversations)
            } else {
                Result.failure(Exception(response.message ?: "Get conversations failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
