package com.example.healthcareapppd.domain.usecase.patient

import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.*

class AddVitalsUseCase {
    private val patientApi = RetrofitClient.patientApi
    
    suspend operator fun invoke(
        token: String,
        heightCm: Double? = null,
        weightKg: Double? = null,
        bloodPressureSystolic: Int? = null,
        bloodPressureDiastolic: Int? = null,
        heartRateBpm: Int? = null,
        temperatureCelsius: Double? = null,
        bloodGlucoseMgDl: Int? = null,
        oxygenSaturationPercent: Int? = null
    ): Result<Vitals> {
        return try {
            val request = AddVitalsRequest(
                heightCm = heightCm,
                weightKg = weightKg,
                bloodPressureSystolic = bloodPressureSystolic,
                bloodPressureDiastolic = bloodPressureDiastolic,
                heartRateBpm = heartRateBpm,
                temperatureCelsius = temperatureCelsius,
                bloodGlucoseMgDl = bloodGlucoseMgDl,
                oxygenSaturationPercent = oxygenSaturationPercent
            )
            val response = patientApi.addVitals("Bearer $token", request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Add vitals failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
