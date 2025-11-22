package com.example.healthcareapppd.presentation.ui.user.Profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthcareapppd.data.api.model.PatientProfile
import com.example.healthcareapppd.domain.usecase.patient.CreatePatientProfileUseCase
import com.example.healthcareapppd.domain.usecase.patient.GetPatientProfileUseCase
import com.example.healthcareapppd.domain.usecase.patient.UpdatePatientProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

data class ProfileUIState(
    val isLoading: Boolean = false,
    val profile: PatientProfile? = null,
    val error: String? = null,
    val success: String? = null,
    val isProfileExists: Boolean = false
)

class PatientProfileViewModel : ViewModel() {
    private val createProfileUseCase = CreatePatientProfileUseCase()
    private val getProfileUseCase = GetPatientProfileUseCase()
    private val updateProfileUseCase = UpdatePatientProfileUseCase()

    private val _uiState = MutableStateFlow(ProfileUIState())
    val uiState: StateFlow<ProfileUIState> = _uiState

    fun getMyProfile(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = getProfileUseCase(token)
            result.onSuccess { profile ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    profile = profile,
                    isProfileExists = true
                )
            }
            result.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Không thể tải hồ sơ",
                    isProfileExists = false
                )
            }
        }
    }

    fun createProfile(
        token: String,
        fullName: String,
        dateOfBirth: String,
        sex: String,
        phoneNumber: String?,
        address: String?,
        emergencyContactName: String?,
        emergencyContactPhone: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = createProfileUseCase(
                token, fullName, dateOfBirth, sex, phoneNumber, address,
                emergencyContactName, emergencyContactPhone
            )
            result.onSuccess { profile ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    profile = profile,
                    success = "Tạo hồ sơ thành công",
                    isProfileExists = true
                )
            }
            result.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Tạo hồ sơ thất bại"
                )
            }
        }
    }

    fun updateProfile(
        token: String,
        fullName: String,
        dateOfBirth: String,
        sex: String,
        phoneNumber: String?,
        address: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            Log.d("PatientProfileVM", "🔄 Updating profile with: fullName=$fullName, dateOfBirth=$dateOfBirth, sex=$sex, phone=$phoneNumber")
            val result = updateProfileUseCase(
                token, fullName, dateOfBirth, sex, phoneNumber, address
            )
            result.onSuccess { profile ->
                Log.d("PatientProfileVM", "✅ Update success: ${profile.fullName}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    profile = profile,
                    success = "Cập nhật hồ sơ thành công"
                )
            }
            result.onFailure { error ->
                Log.e("PatientProfileVM", "❌ Update failed: ${error.message}", error)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Cập nhật hồ sơ thất bại"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, success = null)
    }
}
