package com.example.healthcareapppd.domain.usecase

import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.Period

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

data class BmiWithAdviceResult(
    val bmi: Double,
    val category: String,
    val healthAdvice: String
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

    /**
     * Tính BMI và lấy lời khuyên sức khỏe dựa trên tuổi
     * Cho trẻ em dưới 18 tuổi có lời khuyên riêng
     */
    fun calculateBmiWithAdvice(
        weightInKg: Double,
        heightInCm: Double,
        dateOfBirth: String?
    ): BmiWithAdviceResult? {
        if (weightInKg <= 0 || heightInCm <= 0) {
            return null
        }

        val age = if (dateOfBirth != null) getAgeFromDOB(dateOfBirth) else null
        val bmi = roundToOneDecimal(calculateRawBmi(weightInKg, heightInCm))
        
        val (category, advice) = if (age != null && age < 18) {
            getChildCategoryAndAdvice(bmi, age)
        } else {
            getAdultCategoryAndAdvice(bmi)
        }

        return BmiWithAdviceResult(bmi, category, advice)
    }

    private fun calculateRawBmi(weightInKg: Double, heightInCm: Double): Double {
        val heightInMeters = heightInCm / 100
        return weightInKg / (heightInMeters * heightInMeters)
    }

    private fun getAgeFromDOB(dateOfBirth: String): Int? {
        return try {
            val dob = LocalDate.parse(dateOfBirth) // YYYY-MM-DD
            val today = LocalDate.now()
            Period.between(dob, today).years
        } catch (e: Exception) {
            null
        }
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

    private fun getAdultCategoryAndAdvice(bmi: Double): Pair<String, String> {
        return when {
            bmi < 18.5 -> Pair(
                "underweight",
                "⚠️ Cân nặng của bạn thấp hơn bình thường.\n" +
                "Lời khuyên:\n" +
                "• Tăng cường ăn uống giàu dinh dưỡng\n" +
                "• Bổ sung thêm lượng calo hợp lý hàng ngày\n" +
                "• Tập thể dục thường xuyên để tăng sức khỏe\n" +
                "• Tư vấn với bác sĩ hoặc chuyên gia dinh dưỡng"
            )
            bmi < 25.0 -> Pair(
                "normal",
                "✅ Cân nặng của bạn ở mức bình thường.\n" +
                "Lời khuyên:\n" +
                "• Duy trì chế độ ăn uống cân bằng\n" +
                "• Tập thể dục thường xuyên (150 phút/tuần)\n" +
                "• Kiểm tra sức khỏe định kỳ\n" +
                "• Tránh thực phẩm xử lý nhiều"
            )
            bmi < 30.0 -> Pair(
                "overweight",
                "⚠️ Cân nặng của bạn cao hơn bình thường.\n" +
                "Lời khuyên:\n" +
                "• Giảm cân một cách hợp lý: 0.5-1 kg/tuần\n" +
                "• Tăng hoạt động thể chất đều đặn\n" +
                "• Giảm calo từ thực phẩm có chất béo, đường\n" +
                "• Ăn nhiều rau xanh, trái cây, thực phẩm giàu sợi\n" +
                "• Tư vấn với chuyên gia dinh dưỡng hoặc bác sĩ"
            )
            else -> Pair(
                "obese",
                "🚨 Cân nặng của bạn cao hơn nhiều so với bình thường.\n" +
                "Lời khuyên:\n" +
                "• ⚠️ LIÊN HỆ VỚI BÁC SĨ ĐỂ TƯ VẤN\n" +
                "• Xây dựng kế hoạch giảm cân an toàn với bác sĩ\n" +
                "• Tập thể dục thường xuyên (250+ phút/tuần)\n" +
                "• Thay đổi chế độ ăn uống lành mạnh\n" +
                "• Tư vấn với chuyên gia dinh dưỡng"
            )
        }
    }

    private fun getChildCategoryAndAdvice(bmi: Double, age: Int): Pair<String, String> {
        return when {
            bmi < 16 -> Pair(
                "underweight",
                "⚠️ Cân nặng của bạn thấp hơn bình thường.\n" +
                "Lời khuyên cho trẻ em:\n" +
                "• Tăng cường ăn uống lành mạnh, đủ dinh dưỡng\n" +
                "• Ăn các thực phẩm giàu protein: trứng, sữa, thịt nạc, cá\n" +
                "• Ăn ngũ cốc nguyên hạt, hoa quả, rau xanh\n" +
                "• Uống đủ nước (6-8 ly nước hàng ngày)\n" +
                "• Tập thể dục nhẹ nhàng để xây dựng cơ bắp\n" +
                "• Liên hệ bác sĩ nếu tình trạng không cải thiện"
            )
            bmi < 22 -> Pair(
                "normal",
                "✅ Cân nặng của bạn ở mức bình thường.\n" +
                "Lời khuyên cho trẻ em:\n" +
                "• Duy trì chế độ ăn cân bằng, đủ dinh dưỡng\n" +
                "• Vận động thể thao ít nhất 60 phút mỗi ngày\n" +
                "• Hạn chế đồ ăn nhiều dầu mỡ, đường, muối\n" +
                "• Ngủ đủ 8-10 giờ mỗi đêm\n" +
                "• Uống nước thay vì nước ngọt có ga\n" +
                "• Kiểm tra sức khỏe định kỳ"
            )
            bmi < 26 -> Pair(
                "overweight",
                "⚠️ Cân nặng của bạn cao hơn bình thường.\n" +
                "Lời khuyên cho trẻ em:\n" +
                "• Giảm lượng calo hàng ngày một cách từ từ và hợp lý\n" +
                "• Hạn chế các thực phẩm chứa nhiều calo: bánh kẹo, nước ngọt\n" +
                "• Tăng hoạt động thể chất: chơi thể thao, đi bộ, chạy\n" +
                "• Ăn nhiều trái cây, rau xanh, thực phẩm giàu sợi\n" +
                "• Giảm thời gian xem TV, chơi game\n" +
                "• Uống nước thay vì nước ngọt có ga\n" +
                "• Liên hệ bác sĩ để có kế hoạch giảm cân an toàn"
            )
            else -> Pair(
                "obese",
                "🚨 Cân nặng của bạn cao hơn nhiều so với bình thường.\n" +
                "Lời khuyên cho trẻ em:\n" +
                "• ⚠️ ĐÃ CẦN LIÊN HỆ VỚI BÁC SĨ NGAY\n" +
                "• Bác sĩ sẽ giúp xây dựng kế hoạch giảm cân an toàn\n" +
                "• Tăng hoạt động thể chất dần dần\n" +
                "• Thay đổi chế độ ăn uống với sự hướng dẫn của bác sĩ\n" +
                "• Hạn chế thực phẩm nhanh, đồ ngọt, nước ngọt\n" +
                "• Tập thể dục ít nhất 60 phút mỗi ngày\n" +
                "• Cân nhắc tham khảo ý kiến chuyên gia dinh dưỡng"
            )
        }
    }

    private fun roundToOneDecimal(value: Double): Double {
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.HALF_UP
        return df.format(value).replace(',', '.').toDouble()
    }
}