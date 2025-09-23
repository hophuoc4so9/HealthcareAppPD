package com.example.healthcareapppd.presentation.ui.user.bmi

import androidx.lifecycle.ViewModel
import com.example.healthcareapppd.domain.usecase.BmiResult
import com.example.healthcareapppd.domain.usecase.CalculateBmiUseCase
import com.example.healthcareapppd.domain.usecase.Gender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class BmiUiState(
    val result: BmiResult? = null,
    val showResult: Boolean = false
)

class BmiCalculatorViewModel : ViewModel() {

    // Khởi tạo UseCase.
    private val calculateBmiUseCase = CalculateBmiUseCase()


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

        if (weight == null || height == null || age == null) {
            _uiState.update {
                it.copy(
                    result = BmiResult(0.0, "Lỗi", "Vui lòng nhập đầy đủ và chính xác."),
                    showResult = true
                )
            }
            return
        }

        val bmiResult = calculateBmiUseCase(
            weightInKg = weight,
            heightInCm = height,
            age = age,
            gender = gender
        )

        _uiState.update {
            it.copy(
                result = bmiResult,
                showResult = true
            )
        }
    }
}