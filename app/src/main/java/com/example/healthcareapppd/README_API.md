# PD Health - Android API Integration

## 📦 Các thành phần đã tạo

### 1. API Services (`data/api/`)
Tất cả các API service đã được tạo để giao tiếp với backend:

- **`AuthApiService`** - Authentication (login, register, change password, profile)
- **`PatientApiService`** - Patient profile, vitals, health metrics
- **`DoctorApiService`** - Doctor profile, search, verification
- **`AppointmentApiService`** - Appointments và availability slots
- **`FacilitiesApiService`** - Health facilities (hospitals, pharmacies, clinics)
- **`ReminderApiService`** - Medication và health reminders
- **`ChatApiService`** - Chat với bác sĩ
- **`ArticleApiService`** - Health articles

### 2. Data Models (`data/api/model/ApiModels.kt`)
Tất cả các data class cho request/response:
- User, AuthResponse
- PatientProfile, Vitals, HealthMetric
- DoctorProfile, Appointment, Reminder
- Conversation, ChatMessage, Article
- ApiResponse, PaginatedResponse

### 3. Use Cases (`domain/usecase/`)
Các use case đã được tổ chức theo module:

#### Authentication (`auth/`)
- `LoginUseCase`
- `RegisterUseCase`
- `ChangePasswordUseCase`
- `GetProfileUseCase`
- `VerifyTokenUseCase`

#### Patient (`patient/`)
- `CreatePatientProfileUseCase`
- `GetPatientProfileUseCase`
- `UpdatePatientProfileUseCase`
- `AddVitalsUseCase`
- `GetVitalsHistoryUseCase`
- `GetLatestVitalsUseCase`
- `AddHealthMetricsUseCase`
- `GetHealthMetricsUseCase`
- `GetMetricsSummaryUseCase`

#### Doctor (`doctor/`)
- `GetAllDoctorsUseCase`
- `SearchDoctorsBySpecializationUseCase`
- `GetDoctorByIdUseCase`

#### Appointment (`appointment/`)
- `GetAvailabilitySlotsUseCase`
- `BookAppointmentUseCase`
- `GetMyAppointmentsUseCase`
- `CancelAppointmentUseCase`

#### Reminder (`reminder/`)
- `CreateReminderUseCase`
- `GetMyRemindersUseCase`
- `DeleteReminderUseCase`

#### Chat (`chat/`)
- `CreateConversationUseCase`
- `GetConversationsUseCase`
- `GetMessagesUseCase`
- `SendMessageUseCase`

#### Facility (`facility/`)
- `GetNearestFacilitiesUseCase`
- `SearchFacilitiesUseCase`
- `GetFacilitiesByTypeUseCase`
- `GetFacilityByIdUseCase`

#### Article (`article/`)
- `GetAllArticlesUseCase`
- `GetArticleByIdUseCase`
- `GetArticleBySlugUseCase`

---

## 🚀 Cách sử dụng

### 1. Cấu hình
BASE_URL đã được cập nhật: `https://be-healthcareapppd.onrender.com/`

**Lưu ý:** Server Render có thể mất 30-60 giây để khởi động lần đầu nếu không hoạt động. RetrofitClient đã được cấu hình timeout 60 giây.

### 2. Ví dụ sử dụng

#### Login
```kotlin
val loginUseCase = LoginUseCase()
viewModelScope.launch {
    val result = loginUseCase("user@example.com", "password123")
    result.onSuccess { authResponse ->
        // Lưu token: authResponse.token
        // User info: authResponse.user
    }.onFailure { error ->
        // Xử lý lỗi
    }
}
```

#### Tìm cơ sở y tế gần nhất
```kotlin
val getNearestFacilities = GetNearestFacilitiesUseCase()
viewModelScope.launch {
    val result = getNearestFacilities(
        latitude = 10.7769,
        longitude = 106.7009,
        radius = 5000, // 5km
        limit = 10,
        type = "pharmacy" // hoặc "hospital", "clinic"
    )
    result.onSuccess { facilities ->
        // Hiển thị danh sách cơ sở y tế
    }
}
```

#### Đặt lịch khám
```kotlin
val bookAppointment = BookAppointmentUseCase()
viewModelScope.launch {
    val result = bookAppointment(
        token = "Bearer your_token",
        doctorUserId = "doctor-uuid",
        availabilitySlotId = "slot-uuid",
        patientNotes = "Đau đầu kéo dài 3 ngày"
    )
    result.onSuccess { appointment ->
        // Đặt lịch thành công
    }
}
```

#### Thêm chỉ số sức khỏe
```kotlin
val addVitals = AddVitalsUseCase()
viewModelScope.launch {
    val result = addVitals(
        token = "Bearer your_token",
        heartRateBpm = 72,
        bloodPressureSystolic = 120,
        bloodPressureDiastolic = 80,
        temperatureCelsius = 36.5
    )
}
```

#### Gửi tin nhắn cho bác sĩ
```kotlin
val sendMessage = SendMessageUseCase()
viewModelScope.launch {
    val result = sendMessage(
        token = "Bearer your_token",
        conversationId = "conversation-uuid",
        messageContent = "Chào bác sĩ, em muốn hỏi về kết quả xét nghiệm"
    )
}
```

---

## 📝 Lưu ý quan trọng

### 1. Authentication
Hầu hết các API đều yêu cầu token. Format header:
```
Authorization: Bearer YOUR_JWT_TOKEN
```

Token có hiệu lực **24 giờ**.

### 2. Facility Types
Các loại cơ sở y tế:
- `pharmacy` - Nhà thuốc
- `hospital` - Bệnh viện
- `clinic` - Phòng khám
- `dentist` - Nha khoa
- `doctor` - Bác sĩ

### 3. Reminder Types
Các loại nhắc nhở:
- `medication` - Uống thuốc
- `sleep` - Giấc ngủ
- `appointment` - Lịch hẹn
- `general` - Chung

### 4. Appointment Status
- `scheduled` - Đã đặt lịch
- `completed` - Đã hoàn thành
- `cancelled_by_patient` - Bệnh nhân hủy
- `cancelled_by_doctor` - Bác sĩ hủy

### 5. Metric Types
Các loại health metrics:
- `steps` - Số bước chân
- `sleep_hours` - Giờ ngủ
- `distance_meters` - Quãng đường (mét)
- `active_calories` - Calories tiêu hao

---

## 🔧 Dependencies cần thêm vào `build.gradle.kts`

```kotlin
dependencies {
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // OkHttp Logging
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    
    // Google Maps (cho LatLng)
    implementation("com.google.android.gms:play-services-maps:18.2.0")
}
```

---

## 🌐 API Documentation
Xem chi tiết tại: `app/src/main/java/com/example/healthcareapppd/data/api/# PD HEALTH - COMPLETE API DOCUMENTATION.txt`

---

## ⚠️ Troubleshooting

### Server không phản hồi
Server Render miễn phí sẽ "ngủ" sau 15 phút không hoạt động. Lần gọi đầu tiên có thể mất 30-60 giây để server "thức dậy". Hãy đợi hoặc thử lại.

### Timeout Error
Đã cấu hình timeout 60 giây. Nếu vẫn timeout, kiểm tra:
1. Kết nối Internet
2. Server có đang hoạt động không (mở browser thử: https://be-healthcareapppd.onrender.com)

### Token hết hạn
Token có hiệu lực 24h. Khi hết hạn, cần login lại để lấy token mới.

---

## 📧 Contact
GitHub: hophuoc4so9
