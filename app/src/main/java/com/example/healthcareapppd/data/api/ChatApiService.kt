package com.example.healthcareapppd.data.api

import com.example.healthcareapppd.data.api.model.*
import retrofit2.http.*

interface ChatApiService {
    
    @POST("api/chat/conversations/start")
    suspend fun createConversation(
        @Header("Authorization") token: String,
        @Body request: CreateConversationRequest
    ): ApiResponse<Conversation>
    
    @GET("api/chat/conversations")
    suspend fun getMyConversations(
        @Header("Authorization") token: String
    ): ApiResponse<GetConversationsResponse>
    
    @GET("api/chat/conversations/{conversationId}")
    suspend fun getConversationDetails(
        @Header("Authorization") token: String,
        @Path("conversationId") conversationId: String
    ): ApiResponse<Conversation>
    
    @GET("api/chat/conversations/{conversationId}/messages")
    suspend fun getMessages(
        @Header("Authorization") token: String,
        @Path("conversationId") conversationId: String,
        @Query("limit") limit: Int? = null
    ): ApiResponse<GetMessagesResponse>
    
    @POST("api/chat/conversations/{conversationId}/messages")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Path("conversationId") conversationId: String,
        @Body request: SendMessageRequest
    ): ApiResponse<ChatMessage>
    
    @PATCH("api/chat/messages/{messageId}/read")
    suspend fun markAsRead(
        @Header("Authorization") token: String,
        @Path("messageId") messageId: Long
    ): ApiResponse<Any>
}
