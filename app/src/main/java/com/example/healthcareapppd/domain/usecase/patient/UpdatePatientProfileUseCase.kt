package com.example.healthcareapppd.domain.usecase.patient

import android.util.Log
import com.google.gson.Gson
import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*

class UpdatePatientProfileUseCase {
    private val patientApi = RetrofitClient.patientApi
    private val gson = Gson()
    
    suspend operator fun invoke(
        token: String,
        fullName: String? = null,
        dateOfBirth: String? = null,
        sex: String? = null,
        phoneNumber: String? = null,
        address: String? = null
    ): Result<PatientProfile> {
        return try {
            val request = UpdatePatientProfileRequest(
                fullName = fullName,
                dateOfBirth = dateOfBirth,
                sex = sex,
                phoneNumber = phoneNumber,
                address = address
            )
            
            // Log request JSON để xem field names
            val requestJson = gson.toJson(request)
            Log.d("UpdateProfileUseCase", "📤 Request JSON: $requestJson")
            Log.d("UpdateProfileUseCase", "📤 Request data:")
            Log.d("UpdateProfileUseCase", "   - fullName: $fullName")
            Log.d("UpdateProfileUseCase", "   - dateOfBirth: $dateOfBirth")
            Log.d("UpdateProfileUseCase", "   - sex: $sex")
            Log.d("UpdateProfileUseCase", "   - phoneNumber: $phoneNumber")
            Log.d("UpdateProfileUseCase", "   - address: $address")
            Log.d("UpdateProfileUseCase", "🔗 Token: Bearer $token")
            
            val response = patientApi.updateProfile("Bearer $token", request)
            
            Log.d("UpdateProfileUseCase", "📥 Response: success=${response.success}, message=${response.message}")
            Log.d("UpdateProfileUseCase", "📥 Response data: ${gson.toJson(response.data)}")
            
            if (response.success && response.data != null) {
                Log.d("UpdateProfileUseCase", "✅ Update successful!")
                Result.success(response.data)
            } else {
                val errorMsg = response.message ?: "Update profile failed"
                Log.w("UpdateProfileUseCase", "⚠️ Server error: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("UpdateProfileUseCase", "❌ Exception: ${e.message}", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
