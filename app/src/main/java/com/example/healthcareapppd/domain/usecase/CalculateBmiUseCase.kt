package com.example.healthcareapppd.domain.usecase

import java.math.RoundingMode
import java.text.DecimalFormat

// Enum để định nghĩa giới tính
enum class Gender {
    MALE,
    FEMALE
}

data class BmiResult(
    val score: Double,
    val category: String,
    val message: String
)

class CalculateBmiUseCase {

    operator fun invoke(
        weightInKg: Double,
        heightInCm: Double,
        age: Int,
        gender: Gender
    ): BmiResult {
        // 1. Validate dữ liệu đầu vào
        if (weightInKg <= 0 || heightInCm <= 0 || age <= 0) {
            return BmiResult(0.0, "Không hợp lệ", "Vui lòng kiểm tra lại cân nặng, chiều cao và tuổi.")
        }

        if (age < 2) {
            return BmiResult(0.0, "Không áp dụng", "Chỉ số BMI không được sử dụng cho trẻ em dưới 2 tuổi.")
        }

        val bmiScore = calculateRawBmi(weightInKg, heightInCm)
        val roundedBmi = roundToOneDecimal(bmiScore)

        return if (age >= 20) {
            getAdultResult(roundedBmi)
        } else {
            getChildResult(roundedBmi, age, gender)
        }
    }

    private fun calculateRawBmi(weightInKg: Double, heightInCm: Double): Double {
        val heightInMeters = heightInCm / 100
        return weightInKg / (heightInMeters * heightInMeters)
    }

    private fun getAdultResult(bmi: Double): BmiResult {
        val category: String
        val message: String
        when {
            bmi < 18.5 -> {
                category = "Thiếu cân"
                message = "Bạn có nguy cơ suy dinh dưỡng. Hãy tham khảo ý kiến chuyên gia để cải thiện."
            }
            bmi < 25 -> {
                category = "Bình thường"
                message = "Xin chúc mừng! Bạn có một thân hình cân đối. Hãy tiếp tục duy trì."
            }
            bmi < 30 -> {
                category = "Thừa cân"
                message = "Bạn đang ở mức thừa cân. Hãy xem xét điều chỉnh chế độ ăn và tăng cường vận động."
            }
            else -> {
                category = "Béo phì"
                message = "Tình trạng béo phì có thể gây ra nhiều vấn đề sức khỏe. Cần có kế hoạch giảm cân."
            }
        }
        return BmiResult(bmi, category, message)
    }

    private fun getChildResult(bmi: Double, age: Int, gender: Gender): BmiResult {


        val percentileCategory = when {
            bmi < 16 -> "Thiếu cân"
            bmi < 22 -> "Cân nặng khỏe mạnh"
            bmi < 26 -> "Thừa cân"
            else -> "Béo phì"
        }

        val message = "BMI của trẻ em được đánh giá dựa trên biểu đồ tăng trưởng theo độ tuổi và giới tính. Hãy tham khảo ý kiến bác sĩ nhi khoa để có kết quả chính xác nhất."

        return BmiResult(bmi, percentileCategory, message)
    }

    private fun roundToOneDecimal(value: Double): Double {
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.HALF_UP
        return df.format(value).replace(',', '.').toDouble()
    }
}