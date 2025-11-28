package com.example.healthcareapppd.presentation.ui

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.io.Serializable

object ApiService {
    private const val BASE_URL = "https://be-healthcareapppd.onrender.com/api"
    private val client = OkHttpClient()
    private val gson = Gson()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    // Doctor Dashboard Stats
    fun getDoctorDashboardStats(token: String): ApiResponse<DashboardStats> {
        val request = Request.Builder()
            .url("$BASE_URL/doctors/dashboard/stats")
            .header("Authorization", "Bearer $token")
            .build()

        return executeRequest(request)
    }

    // Doctor appointments
    fun getDoctorAppointments(token: String, status: String?): ApiResponse<AppointmentsData> {
        val url = if (status != null) {
            "$BASE_URL/appointments?status=$status"
        } else {
            "$BASE_URL/appointments"
        }

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .build()

        return executeRequest(request)
    }
    fun getAdminDashboardStats(token: String): ApiResponse<AdminDashboardData> {
        val request = Request.Builder()
            .url("https://be-healthcareapppd.onrender.com/api/admin/dashboard")
            .header("Authorization", "Bearer $token")
            .build()
        return executeRequest(request)
    }

    // Get patient detail
    fun getPatientDetail(token: String, patientId: String): ApiResponse<Patient> {
        val request = Request.Builder()
            .url("$BASE_URL/doctors/patients/$patientId")
            .header("Authorization", "Bearer $token")
            .build()

        return executeRequest(request)
    }

    // Get patient appointments
    fun getPatientAppointments(token: String, patientId: String): ApiResponse<AppointmentsData> {
        val request = Request.Builder()
            .url("$BASE_URL/doctors/patients/$patientId/appointments")
            .header("Authorization", "Bearer $token")
            .build()

        return executeRequest(request)
    }

    // Get doctor patients
    fun getDoctorPatients(token: String, limit: Int = 1000): ApiResponse<List<Patient>> {
        val request = Request.Builder()
            .url("$BASE_URL/doctors/patients?limit=$limit")
            .header("Authorization", "Bearer $token")
            .build()

        return executeRequest(request)
    }

    // Chat - Get conversations
    fun getMyConversations(token: String): ApiResponse<ConversationsData> {
        val request = Request.Builder()
            .url("$BASE_URL/chat/conversations")
            .header("Authorization", "Bearer $token")
            .build()

        return executeRequest(request)
    }

    // Chat - Get messages
    fun getConversationMessages(token: String, conversationId: String): ApiResponse<MessagesData> {
        val request = Request.Builder()
            .url("$BASE_URL/chat/conversations/$conversationId/messages")
            .header("Authorization", "Bearer $token")
            .build()

        return executeRequest(request)
    }

    // Chat - Send message
    fun sendMessage(token: String, conversationId: String, messageContent: String): ApiResponse<MessageData> {
        val json = gson.toJson(mapOf("messageContent" to messageContent))
        val body = json.toRequestBody(JSON)

        val request = Request.Builder()
            .url("$BASE_URL/chat/conversations/$conversationId/messages")
            .header("Authorization", "Bearer $token")
            .post(body)
            .build()

        return executeRequest(request)
    }

    // Create conversation
    fun createConversation(token: String, withUserId: String): ApiResponse<ConversationData> {
        val json = gson.toJson(mapOf("withUserId" to withUserId))
        val body = json.toRequestBody(JSON)

        val request = Request.Builder()
            .url("$BASE_URL/chat/conversations/start")
            .header("Authorization", "Bearer $token")
            .post(body)
            .build()

        return executeRequest(request)
    }

    // Update appointment status
    fun updateAppointmentStatus(token: String, appointmentId: String, status: String): ApiResponse<AppointmentData> {
        val json = gson.toJson(mapOf("status" to status))
        val body = json.toRequestBody(JSON)

        val request = Request.Builder()
            .url("$BASE_URL/appointments/$appointmentId/status")
            .header("Authorization", "Bearer $token")
            .patch(body)
            .build()

        return executeRequest(request)
    }
    // ==================== ADMIN APIs ====================

    // 1. Get All Users
    fun getAllUsers(token: String): ApiResponse<UsersData> {
        val request = Request.Builder()
            .url("$BASE_URL/users")
            .header("Authorization", "Bearer $token")
            .build()
        return executeRequest(request)
    }

    // 2. Ban/Unban User
    fun banUnbanUser(token: String, userId: String, currentStatusIsBanned: Boolean): ApiResponse<Any> {
        // Logic: Nếu đang ban thì gọi unban, ngược lại gọi ban
        val action = if (currentStatusIsBanned) "unban" else "ban"

        val request = Request.Builder()
            .url("$BASE_URL/users/$userId/$action")
            .header("Authorization", "Bearer $token")
            .patch("{}".toRequestBody(JSON)) // Body rỗng
            .build()
        return executeRequest(request)
    }

    // 3. Update User Role
    fun updateUserRole(token: String, userId: String, newRole: String): ApiResponse<Any> {
        val json = gson.toJson(mapOf("role" to newRole))
        val body = json.toRequestBody(JSON)

        val request = Request.Builder()
            .url("$BASE_URL/users/$userId")
            .header("Authorization", "Bearer $token")
            .put(body) // Web dùng PUT
            .build()
        return executeRequest(request)
    }

    // 3. Doctors Verification
    fun getPendingDoctors(token: String): ApiResponse<DoctorsData> {
        val request = Request.Builder()
            .url("$BASE_URL/doctors?status=pending")
            .header("Authorization", "Bearer $token")
            .build()
        return executeRequest(request)
    }

    fun verifyDoctor(token: String, userId: String, status: String): ApiResponse<Any> {
        // status: 'approved' | 'rejected'
        val json = gson.toJson(mapOf("status" to status))
        val body = json.toRequestBody(JSON)
        val request = Request.Builder()
            .url("$BASE_URL/doctors/$userId/verification")
            .header("Authorization", "Bearer $token")
            .patch(body)
            .build()
        return executeRequest(request)
    }

    // 4. Articles Management
    fun getArticles(token: String): ApiResponse<ArticlesData> {
        val request = Request.Builder()
            .url("$BASE_URL/articles")
            .header("Authorization", "Bearer $token")
            .build()
        return executeRequest(request)
    }

    // 2. Create Article
    fun createArticle(token: String, article: Map<String, String>): ApiResponse<Article> {
        val body = gson.toJson(article).toRequestBody(JSON)
        val request = Request.Builder()
            .url("$BASE_URL/articles")
            .header("Authorization", "Bearer $token")
            .post(body)
            .build()
        return executeRequest(request)
    }

    // 3. Update Article
    fun updateArticle(token: String, id: String, article: Map<String, String>): ApiResponse<Article> {
        val body = gson.toJson(article).toRequestBody(JSON)
        val request = Request.Builder()
            .url("$BASE_URL/articles/$id")
            .header("Authorization", "Bearer $token")
            .put(body) // Web dùng PUT
            .build()
        return executeRequest(request)
    }

    // 4. Delete Article
    fun deleteArticle(token: String, id: String): ApiResponse<Any> {
        val request = Request.Builder()
            .url("$BASE_URL/articles/$id")
            .header("Authorization", "Bearer $token")
            .delete()
            .build()
        return executeRequest(request)
    }

    // Generic request executor
    private inline fun <reified T> executeRequest(request: Request): ApiResponse<T> {
        return try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string()

                if (!response.isSuccessful) {
                    return ApiResponse(
                        success = false,
                        data = gson.fromJson("{}", T::class.java),
                        message = "HTTP ${response.code}: ${body ?: "Unknown error"}"
                    )
                }

                val type = object : TypeToken<ApiResponse<T>>() {}.type
                gson.fromJson<ApiResponse<T>>(body, type)
            }
        } catch (e: IOException) {
            ApiResponse(
                success = false,
                data = gson.fromJson("{}", T::class.java),
                message = "Network error: ${e.message}"
            )
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                data = gson.fromJson("{}", T::class.java),
                message = "Error: ${e.message}"
            )
        }
    }
}

// ==================== DATA MODELS ====================

// Response wrapper
data class ApiResponse<T>(
    val success: Boolean,
    val data: T,
    val message: String? = null
)

// Appointment models
// Cập nhật Model Appointment để dùng chung cho cả Doctor và Admin
data class Appointment(
    val id: String = "",
    val status: String = "",

    // Các trường chung
    val patientName: String? = null,
    val patientEmail: String? = null,
    val patientUserId: String? = null,
    val doctorUserId: String? = null,

    // Các trường ghi chú
    val notes: String? = null,
    val patientNotes: String? = null,
    val doctorNotes: String? = null,

    // --- CÁC TRƯỜNG THỜI GIAN (Doctor API trả về slot..., Admin trả về start...) ---
    val appointmentDate: String? = null, // Doctor
    val slotStartTime: String? = null,   // Doctor
    val slotEndTime: String? = null,     // Doctor

    // --- CÁC TRƯỜNG DÀNH RIÊNG CHO ADMIN DASHBOARD (Fix lỗi unresolved) ---
    val doctorName: String? = null,      // Fix lỗi doctorName
    val startTime: String? = null,       // Fix lỗi startTime
    val endTime: String? = null,
    val createdAt: String? = null
) : Serializable
data class AppointmentData(
    val appointment: Appointment? = null
)

data class AppointmentsData(
    val appointments: List<Appointment> = emptyList(),
    val count: Int = 0
)

// Patient models
data class Patient(
    val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String? = null,
    val dateOfBirth: String? = null,
    val sex: String? = null,         // JSON dùng "sex"
    val address: String? = null,

    // JSON trả về String ("1"), không phải Int
    val totalAppointments: String? = "0",
    val completedAppointments: String? = "0",
    val upcomingAppointments: String? = "0",

    val lastAppointmentDate: String? = null,
    val createdAt: String? = null
) : Serializable

// Class này có thể không cần dùng cho getPatientDetail nữa, nhưng giữ lại để tránh lỗi code cũ
data class PatientData(
    val patient: Patient? = null
)

// Conversation models
data class Conversation(
    val id: String = "",
    val patientUserId: String = "",
    val patientName: String = "",
    val patientEmail: String = "",
    val lastMessage: String? = null,
    val lastMessageTime: String? = null,
    val unreadCount: Int = 0
)

data class ConversationData(
    val conversation: Conversation? = null
)

data class ConversationsData(
    val conversations: List<Conversation> = emptyList(),
    val count: Int = 0
)

// Message models
data class Message(
    val id: String = "",
    val conversationId: String = "",
    val senderUserId: String = "",
    val messageContent: String = "",
    val isRead: Boolean = false,
    val createdAt: String = ""
)

data class MessageData(
    val message: Message? = null
)

data class MessagesData(
    val messages: List<Message> = emptyList(),
    val count: Int = 0
)

// Dashboard Stats
data class DashboardStats(
    val totalPatients: Int = 0,
    val totalAppointments: Int = 0,
    val upcomingAppointments: Int = 0,
    val completedAppointments: Int = 0,
    val todayAppointments: Int = 0,
    val pendingAppointments: Int = 0
)
// 1. Model cho Admin Dashboard (Khớp JSON bạn gửi)
data class AdminDashboardData(
    val stats: AdminStats? = null,
    val recentUsers: List<User>? = null,
    val recentAppointments: List<Appointment>? = null
)

data class AdminStats(
    val totalPatients: String? = "0",
    val totalDoctors: String? = "0",
    val totalAppointments: String? = "0",
    val pendingAppointments: String? = "0",
    val pendingVerifications: String? = "0",
    val publishedArticles: String? = "0"
)

data class User(
    val id: String = "",
    val email: String = "",
    val role: String = "", // patient, doctor, admin
    val is_active: Boolean = false,
    val is_banned: Boolean = false,
    val created_at: String = ""
) : Serializable

data class UsersData(
    val users: List<User> = emptyList()
)

data class DoctorInfo(
    val user_id: String = "",
    val full_name: String = "",
    val email: String = "",
    val specialization: String = "",
    val status: String = "",
    val registered_at: String = ""
)

data class DoctorsData(
    val doctors: List<DoctorInfo> = emptyList()
)

data class Article(
    val id: String = "",
    val title: String = "",
    val status: String = "",
    val created_at: String = ""
)

data class ArticlesData(
    val articles: List<Article> = emptyList()
)