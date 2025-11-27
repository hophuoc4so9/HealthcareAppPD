package com.example.healthcareapppd.domain.repository

interface BmiAiRepository {
    suspend fun getHealthAdvice(bmi: Double, category: String, age: Int, gender: String): String
}