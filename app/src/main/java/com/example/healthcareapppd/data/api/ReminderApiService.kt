package com.example.healthcareapppd.data.api

import com.example.healthcareapppd.data.api.model.*
import retrofit2.http.*

interface ReminderApiService {
    
    @POST("api/reminders")
    suspend fun createReminder(
        @Header("Authorization") token: String,
        @Body request: CreateReminderRequest
    ): ApiResponse<Reminder>
    
    @GET("api/reminders")
    suspend fun getMyReminders(
        @Header("Authorization") token: String
    ): RemindersResponse
    
    @PUT("api/reminders/{id}")
    suspend fun updateReminder(
        @Header("Authorization") token: String,
        @Path("id") reminderId: String,
        @Body request: UpdateReminderRequest
    ): ApiResponse<Reminder>
    
    @PATCH("api/reminders/{id}/toggle")
    suspend fun toggleActive(
        @Header("Authorization") token: String,
        @Path("id") reminderId: String,
        @Body request: ToggleActiveRequest
    ): ApiResponse<Reminder>
    
    @DELETE("api/reminders/{id}")
    suspend fun deleteReminder(
        @Header("Authorization") token: String,
        @Path("id") reminderId: String
    ): ApiResponse<Any>
}
