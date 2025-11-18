package com.example.healthcareapppd.domain.usecase

data class User(
    val id: Int = 0,
    val username: String,
    val phone: String,
    val email: String,
    val password: String
)