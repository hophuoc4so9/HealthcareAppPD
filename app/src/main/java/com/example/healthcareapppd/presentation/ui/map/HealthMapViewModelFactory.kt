package com.example.healthcareapppd.presentation.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.healthcareapppd.domain.repository.FacilitiesRepository


class HealthMapViewModelFactory(
    private val repository: FacilitiesRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HealthMapViewModel::class.java)) {
            return HealthMapViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}