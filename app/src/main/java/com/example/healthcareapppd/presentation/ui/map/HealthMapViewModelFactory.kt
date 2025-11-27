package com.example.healthcareapppd.presentation.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.healthcareapppd.domain.repository.FacilitiesRepository
import com.example.healthcareapppd.utils.SemanticSearchEngine

class HealthMapViewModelFactory(
    private val repository: FacilitiesRepository,
    private val searchEngine: SemanticSearchEngine
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HealthMapViewModel::class.java)) {
            return HealthMapViewModel(repository, searchEngine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}