package com.example.healthcareapppd.data.api

import com.example.healthcareapppd.data.api.model.*
import retrofit2.http.*

interface AppointmentApiService {
    
    // ==================== AVAILABILITY (Doctor) ====================
    @POST("api/appointments/availability")
    suspend fun createAvailabilitySlot(
        @Header("Authorization") token: String,
        @Body request: CreateAvailabilitySlotRequest
    ): ApiResponse<AvailabilitySlot>
    
    @GET("api/appointments/availability")
    suspend fun getMyAvailability(
        @Header("Authorization") token: String,
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null,
        @Query("is_booked") isBooked: Boolean? = null
    ): ApiResponse<List<AvailabilitySlot>>
    
    @DELETE("api/appointments/availability/{id}")
    suspend fun deleteAvailability(
        @Header("Authorization") token: String,
        @Path("id") slotId: String
    ): ApiResponse<Any>
    
    // Patient - View doctor available slots
    @GET("api/appointments/doctors/{doctorUserId}/available-slots")
    suspend fun getDoctorAvailableSlots(
        @Header("Authorization") token: String,
        @Path("doctorUserId") doctorUserId: String,
        @Query("date") date: String? = null
    ): ApiResponse<DoctorAvailableSlotsResponse>
    
    @GET("api/appointments/doctors/{doctorUserId}/available-slots/range")
    suspend fun getDoctorAvailableSlotsRange(
        @Header("Authorization") token: String,
        @Path("doctorUserId") doctorUserId: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): ApiResponse<DoctorAvailableSlotsResponse>
    
    // ==================== APPOINTMENTS ====================
    @POST("api/appointments")
    suspend fun bookAppointment(
        @Header("Authorization") token: String,
        @Body request: BookAppointmentRequest
    ): ApiResponse<Appointment>
    
    @GET("api/appointments")
    suspend fun getMyAppointments(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null,
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null
    ): ApiResponse<GetAppointmentsResponse>
    
    @GET("api/appointments/{id}")
    suspend fun getAppointmentDetails(
        @Header("Authorization") token: String,
        @Path("id") appointmentId: String
    ): ApiResponse<Appointment>
    
    @PATCH("api/appointments/{id}/status")
    suspend fun updateStatus(
        @Header("Authorization") token: String,
        @Path("id") appointmentId: String,
        @Body request: UpdateAppointmentStatusRequest
    ): ApiResponse<Appointment>
    
    @PATCH("api/appointments/{id}/cancel")
    suspend fun cancelAppointment(
        @Header("Authorization") token: String,
        @Path("id") appointmentId: String
    ): ApiResponse<Appointment>
}
