package com.example.healthcareapppd.domain.usecase

import androidx.annotation.DrawableRes
import java.io.Serializable // Import Serializable

data class DoctorUsecase(
    @DrawableRes val photo: Int,
    val name: String,
    val speciality: String,
    val rating: Float,
    val distance: String
) : Serializable