package com.example.healthcareapppd.data.repository

import com.example.healthcareapppd.domain.repository.BmiAiRepository
import com.google.ai.client.generativeai.GenerativeModel

class GeminiBmiRepository(private val apiKey: String) : BmiAiRepository {
    
    // Sử dụng model Gemini Flash cho tốc độ nhanh và miễn phí
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = apiKey
    )

    override suspend fun getHealthAdvice(bmi: Double, category: String, age: Int, gender: String): String {
        return try {
            val prompt = """
                Bạn là một chuyên gia dinh dưỡng và bác sĩ thể thao. 
                Người dùng có thông tin sau:
                - Chỉ số BMI: $bmi
                - Phân loại: $category
                - Tuổi: $age
                - Giới tính: $gender
                
                Hãy đưa ra lời khuyên sức khỏe ngắn gọn, súc tích (dưới 150 từ).
                Chia làm 2 phần: 
                1. Chế độ ăn uống.
                2. Chế độ tập luyện.
                Văn phong thân thiện, khích lệ. Dùng emoji phù hợp.
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            response.text ?: "Không thể lấy lời khuyên từ AI lúc này."
        } catch (e: Exception) {
            e.printStackTrace()
            "Lỗi kết nối AI. Vui lòng kiểm tra mạng."
        }
    }
}