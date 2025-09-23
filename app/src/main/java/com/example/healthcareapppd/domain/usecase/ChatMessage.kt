package com.example.healthcareapppd.domain.usecase

data class ChatMessage(
    val message: String,
    val isSender: Boolean // true = mình gửi (user), false = nhận (doctor)
)

