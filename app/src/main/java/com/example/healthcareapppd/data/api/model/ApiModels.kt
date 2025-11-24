package com.example.healthcareapppd.data.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// ==================== COMMON ====================
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val error: String? = null
)

// ==================== AUTH ====================
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val role: String // "patient", "doctor", "admin"
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class VerifyTokenRequest(
    val token: String
)

data class AuthResponse(
    val token: String,
    val user: User
)

data class User(
    val id: String,
    val email: String,
    val role: String,
    val isActive: Boolean,
    val createdAt: String
)

// ==================== PATIENT ====================
data class CreatePatientProfileRequest(
    val fullName: String,
    val dateOfBirth: String, // YYYY-MM-DD
    val sex: String, // "male", "female", "other", "prefer_not_to_say"
    val phoneNumber: String? = null,
    val address: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null
)

data class UpdatePatientProfileRequest(
    val fullName: String? = null,
    val dateOfBirth: String? = null,
    val sex: String? = null,
    val phoneNumber: String? = null,
    val address: String? = null
)

data class PatientProfile(
    val userId: String,
    val fullName: String,
    val dateOfBirth: String,
    val sex: String,
    val phoneNumber: String?,
    val address: String?,
    val emergencyContactName: String?,
    val emergencyContactPhone: String?,
    val email: String? = null,
    val role: String? = null,
    val isActive: Boolean? = null,
    val userCreatedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class AddVitalsRequest(
    val heightCm: Double? = null,
    val weightKg: Double? = null,
    val bloodPressureSystolic: Int? = null,
    val bloodPressureDiastolic: Int? = null,
    val heartRateBpm: Int? = null,
    val temperatureCelsius: Double? = null,
    val bloodGlucoseMgDl: Int? = null,
    val oxygenSaturationPercent: Int? = null
)

data class Vitals(
    val id: Long,
    val patientUserId: String,
    val heartRateBpm: Int?,
    val bloodPressureSystolic: Int?,
    val bloodPressureDiastolic: Int?,
    val temperatureCelsius: Double?,
    val weightKg: Double?,
    val heightCm: Double?,
    val bloodGlucoseMgDl: Int?,
    val oxygenSaturationPercent: Int?,
    val bmi: Double?,
    val bmiCategory: String?, // "underweight", "normal", "overweight", "obese"
    val recordedAt: String
)

data class AddMetricsRequest(
    val metricType: String, // "steps", "sleep_duration_minutes", "distance_meters", "active_calories"
    val value: Double,
    val startTime: String,
    val endTime: String
)

data class UpdateMetricsRequest(
    val metricType: String? = null,
    val value: Double? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val source: String? = null,
    val metadata: String? = null
)

data class HealthMetric(
    val id: String,
    val patientUserId: String,
    val metricType: String,
    val value: String,
    val startTime: String,
    val endTime: String,
    val source: String,
    val metadata: String? = null
)

data class MetricsData(
    val metrics: List<HealthMetric>,
    val count: Int
)

data class MetricsSummary(
    val metricType: String,
    val totalValue: Double,
    val averageValue: Double,
    val minValue: Double,
    val maxValue: Double,
    val count: Int,
    val fromDate: String?,
    val toDate: String?
)

// ==================== DOCTOR ====================
data class CreateDoctorProfileRequest(
    val fullName: String,
    val specialization: String,
    val licenseNumber: String,
    val yearsOfExperience: Int? = null,
    val hospitalAffiliation: String? = null,
    val phone: String? = null,
    val address: String? = null
)

data class DoctorProfile(
    val userId: String,
    val fullName: String,
    val specialization: String,
    val licenseNumber: String? = null,
    val medicalLicenseId: String? = null,
    val yearsOfExperience: Int? = null,
    val hospitalAffiliation: String? = null,
    val clinicAddress: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val bio: String? = null,
    val verificationStatus: String? = null,
    val status: String? = null,
    val verificationNotes: String? = null,
    val adminNotes: String? = null,
    val email: String? = null,
    val isActive: Boolean? = null,
    val createdAt: String? = null,
    val userCreatedAt: String? = null,
    val updatedAt: String? = null
) : Serializable

data class DoctorsData(
    val doctors: List<DoctorProfile>,
    val pagination: Pagination? = null
)

data class DoctorsResponse(
    val success: Boolean,
    val data: DoctorsData?
)

data class UpdateVerificationStatusRequest(
    val verificationStatus: String,
    val verificationNotes: String? = null
)

// ==================== APPOINTMENTS ====================
data class CreateAvailabilitySlotRequest(
    val startTime: String, // ISO 8601 format
    val endTime: String // ISO 8601 format
)

data class AvailabilitySlot(
    val id: String,
    val doctorUserId: String,
    val startTime: String,
    val endTime: String,
    val isBooked: Boolean,
    val createdAt: String? = null
)

data class DoctorAvailableSlotsResponse(
    val doctorUserId: String,
    val date: String? = null,
    val slots: List<AvailabilitySlot>?,
    val count: Int
)

data class BookAppointmentRequest(
    val doctorUserId: String,
    val availabilitySlotId: String,
    val patientNotes: String? = null
)

data class Appointment(
    val id: String,
    val patientUserId: String,
    val doctorUserId: String,
    val availabilitySlotId: String,
    val status: String, // "scheduled", "completed", "cancelled", "no_show"
    val patientNotes: String?,
    val doctorNotes: String?,
    val createdAt: String,
    val patientName: String?,
    val doctorName: String?,
    val specialization: String?,
    val startTime: String?,
    val endTime: String?
)

data class GetAppointmentsResponse(
    val appointments: List<Appointment>,
    val count: Int
)

data class UpdateAppointmentStatusRequest(
    val status: String,
    val doctorNotes: String? = null
)

// ==================== REMINDERS ====================
data class CreateReminderRequest(
    val title: String,
    val description: String? = null,
    val reminderType: String, // "medication", "sleep", "appointment", "general"
    val cronExpression: String? = null, // For recurring reminders: "0 8 * * *"
    val oneTimeAt: String? = null,
    val timezoneName: String = "Asia/Ho_Chi_Minh"
)

data class UpdateReminderRequest(
    val title: String? = null,
    val description: String? = null,
    val reminderType: String? = null,
    val cronExpression: String? = null,
    val oneTimeAt: String? = null,
    val timezoneName: String? = null
)

data class ToggleActiveRequest(
    val isActive: Boolean
)

data class Reminder(
    val id: String,
    val patientUserId: String,
    val reminderType: String,
    val title: String,
    val description: String?,
    val cronExpression: String?,
    val oneTimeAt: String?,
    val timezoneName: String?,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String? = null
)

data class RemindersData(
    val reminders: List<Reminder>,
    val count: Int
)

data class RemindersResponse(
    val success: Boolean,
    val data: RemindersData?
)

// ==================== CHAT ====================
data class CreateConversationRequest(
    val targetUserId: String
)

data class Conversation(
    val id: String,
    val patientUserId: String,
    val doctorUserId: String,
    val patientName: String?,
    val doctorName: String?,
    val patientEmail: String?,
    val doctorEmail: String?,
    val lastMessage: String? = null,
    val lastMessageTime: String? = null,
    val unreadCount: Int = 0
)

data class GetConversationsResponse(
    val conversations: List<Conversation>,
    val count: Int
)

data class SendMessageRequest(
    val messageContent: String
)

data class ChatMessage(
    val id: Long,
    val conversationId: String,
    val senderUserId: String,
    val messageContent: String,
    val sentAt: String,
    val readAt: String?,
    val senderEmail: String?,
    val senderRole: String?
)

data class GetMessagesResponse(
    val messages: List<ChatMessage>,
    val count: Int
)

// ==================== ARTICLES ====================
data class CreateArticleRequest(
    val title: String,
    val slug: String? = null,
    val contentBody: String? = null,
    val externalUrl: String? = null,
    val featuredImageUrl: String? = null
)

data class Article(
    val id: String,
    val authorAdminId: String?,
    val title: String,
    val slug: String,
    val contentBody: String?,
    val content: String?,
    val externalUrl: String?,
    val featuredImageUrl: String?,
    val status: String,
    val publishedAt: String?,
    val createdAt: String,
    val updatedAt: String?,
    val authorEmail: String?
) : Serializable

data class ArticlesData(
    val articles: List<Article>,
    val pagination: Pagination
)

data class ArticlesResponse(
    val success: Boolean,
    val data: ArticlesData?
)

// ==================== FACILITIES ====================
data class Facility(
    val id: Int,
    val name: String?,
    val type: String,
    val address: String?,
    val geom: String,
    val distanceMeters: Int? = null
)

data class FacilityInAreaRequest(
    val polygon: List<List<Double>>, // [[lng, lat], [lng, lat], ...]
    val type: String? = null,
    val limit: Int? = null
)

data class FacilityStatsResponse(
    val success: Boolean,
    val data: FacilityStats
)

data class FacilityStats(
    val total: Int,
    val byType: Map<String, Int>,
    val cities: List<String>
)

data class FacilityDetailResponse(
    val success: Boolean,
    val data: Facility
)

// ==================== PAGINATION ====================
data class PaginatedResponse<T>(
    val success: Boolean,
    val data: List<T>,
    val pagination: Pagination? = null
)

data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
)
