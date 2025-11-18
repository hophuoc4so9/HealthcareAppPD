package com.example.healthcareapppd.domain.usecase

data class DateSlot(
    val fullDate: String,
    val dayOfWeek: String,
    val date: String,
    var isSelected: Boolean = false
)