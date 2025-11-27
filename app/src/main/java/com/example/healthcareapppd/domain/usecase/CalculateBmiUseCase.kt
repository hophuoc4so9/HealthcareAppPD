package com.example.healthcareapppd.domain.usecase

import com.example.healthcareapppd.domain.repository.BmiAiRepository
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.Period

// Giữ nguyên Enum
enum class Gender { MALE, FEMALE }

// Data class cho kết quả tính toán nhanh (Offline)
data class BmiCalculation(
    val bmi: Double,
    val category: String,
    val basicMessage: String // Lời khuyên cứng (fallback)
)

// Data class cho kết quả đầy đủ từ AI (Online)
data class BmiAiAdvice(
    val aiAdvice: String
)

class CalculateBmiUseCase(
    private val bmiAiRepository: BmiAiRepository // Inject Repository vào đây
) {


    fun calculate(
        weightInKg: Double,
        heightInCm: Double,
        age: Int,
        gender: Gender
    ): BmiCalculation {
        // Validate
        if (weightInKg <= 0 || heightInCm <= 0 || age <= 0) {
            return BmiCalculation(0.0, "Không hợp lệ", "Dữ liệu không đúng.")
        }

        val bmiScore = calculateRawBmi(weightInKg, heightInCm)
        val roundedBmi = roundToOneDecimal(bmiScore)

        // Logic phân loại cứng (Offline)
        val (category, basicMsg) = if (age < 18) {
            getChildCategoryAndAdvice(roundedBmi)
        } else {
            getAdultCategoryAndAdvice(roundedBmi)
        }

        return BmiCalculation(roundedBmi, category, basicMsg)
    }


    suspend fun getAiAdvice(
        bmi: Double,
        category: String,
        age: Int,
        gender: Gender
    ): String {
        val genderStr = if (gender == Gender.MALE) "Nam" else "Nữ"
        return bmiAiRepository.getHealthAdvice(bmi, category, age, genderStr)
    }

    // --- CÁC HÀM PRIVATE HỖ TRỢ (GIỮ NGUYÊN LOGIC CỦA BẠN) ---

    private fun calculateRawBmi(weightInKg: Double, heightInCm: Double): Double {
        val heightInMeters = heightInCm / 100
        return weightInKg / (heightInMeters * heightInMeters)
    }

    private fun roundToOneDecimal(value: Double): Double {
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.HALF_UP
        return df.format(value).replace(',', '.').toDouble()
    }

    // Rút gọn logic cứng để code đỡ dài (fallback khi không có mạng)
    private fun getAdultCategoryAndAdvice(bmi: Double): Pair<String, String> {
        return when {
            bmi < 18.5 -> Pair("Thiếu cân", "Cần bổ sung dinh dưỡng.")
            bmi < 25.0 -> Pair("Bình thường", "Giữ vững phong độ nhé!")
            bmi < 30.0 -> Pair("Thừa cân", "Nên vận động nhiều hơn.")
            else -> Pair("Béo phì", "Cần có chế độ giảm cân nghiêm túc.")
        }
    }

    private fun getChildCategoryAndAdvice(bmi: Double): Pair<String, String> {
        return when {
            bmi < 16 -> Pair("Thiếu cân", "Cần quan tâm dinh dưỡng cho bé.")
            bmi < 22 -> Pair("Khỏe mạnh", "Bé phát triển tốt.")
            bmi < 26 -> Pair("Thừa cân", "Hạn chế đồ ngọt cho bé.")
            else -> Pair("Béo phì", "Cần tư vấn bác sĩ nhi khoa.")
        }
    }
}