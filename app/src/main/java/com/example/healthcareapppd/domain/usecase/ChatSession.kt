package com.example.healthcareapppd.domain.usecase

class ChatSession(
    val id: String,
    val name: String,       // Tên bác sĩ hoặc tên người dùng
    val lastMessage: String,// Tin nhắn cuối cùng
    val isOngoing: Boolean  // true = đang tiếp diễn, false = đã đóng
)