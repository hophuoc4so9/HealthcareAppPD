package com.example.healthcareapppd.presentation.ui.user.bmi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.healthcareapppd.domain.repository.BmiAiRepository
import com.example.healthcareapppd.domain.usecase.CalculateBmiUseCase
import com.example.healthcareapppd.domain.usecase.Gender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// State UI: Quản lý toàn bộ trạng thái màn hình
data class BmiUiState(
    val bmi: Double = 0.0,
    val category: String = "",
    val advice: String = "",       // Lời khuyên (Cơ bản hoặc từ AI)
    val showResult: Boolean = false,
    val isAiLoading: Boolean = false, // Trạng thái đang tải AI
    val error: String? = null
)

class BmiCalculatorViewModel(
    private val calculateBmiUseCase: CalculateBmiUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BmiUiState())
    val uiState: StateFlow<BmiUiState> = _uiState.asStateFlow()

    fun calculateBmi(
        weightStr: String,
        heightStr: String,
        ageStr: String,
        gender: Gender
    ) {
        val weight = weightStr.toDoubleOrNull()
        val height = heightStr.toDoubleOrNull()
        val age = ageStr.toIntOrNull()

        // 1. Validate dữ liệu
        if (weight == null || height == null || age == null) {
            _uiState.update {
                it.copy(error = "Vui lòng nhập đầy đủ và chính xác số liệu.", showResult = false)
            }
            return
        }

        viewModelScope.launch {
            // 2. Reset trạng thái & Hiển thị Loading AI
            _uiState.update {
                it.copy(showResult = true, isAiLoading = true, error = null)
            }

            // 3. TÍNH TOÁN OFFLINE (Có kết quả ngay lập tức)
            // Lưu ý: Hàm calculate này là hàm bạn đã viết ở UseCase (trả về BmiCalculation)
            val offlineResult = calculateBmiUseCase.calculate(weight, height, age, gender)

            _uiState.update {
                it.copy(
                    bmi = offlineResult.bmi,
                    category = offlineResult.category,
                    advice = offlineResult.basicMessage // Hiển thị lời khuyên cứng trước
                )
            }

            // 4. GỌI AI ONLINE (Chạy ngầm để lấy lời khuyên xịn)
            try {
                val aiAdvice = calculateBmiUseCase.getAiAdvice(
                    offlineResult.bmi,
                    offlineResult.category,
                    age,
                    gender
                )

                // Cập nhật lại UI với lời khuyên từ AI
                _uiState.update {
                    it.copy(
                        advice = aiAdvice,
                        isAiLoading = false
                    )
                }
            } catch (e: Exception) {
                // Nếu lỗi AI, tắt loading, giữ nguyên lời khuyên cơ bản
                _uiState.update { it.copy(isAiLoading = false) }
                e.printStackTrace()
            }
        }
    }
}

// Factory để khởi tạo ViewModel với tham số (UseCase)
class BmiViewModelFactory(
    private val useCase: CalculateBmiUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BmiCalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BmiCalculatorViewModel(useCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}