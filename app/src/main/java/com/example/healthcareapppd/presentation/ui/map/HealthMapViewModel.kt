package com.example.healthcareapppd.presentation.ui.map

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthcareapppd.data.api.*
import com.example.healthcareapppd.data.api.getLatLng
import com.example.healthcareapppd.data.api.model.Facility
import com.example.healthcareapppd.domain.repository.FacilitiesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class FacilitiesUiState {
    object Loading : FacilitiesUiState()
    data class Success(val facilities: List<Facility>) : FacilitiesUiState()
    data class Error(val message: String) : FacilitiesUiState()
}

// Extension function để tính khoảng cách
fun Facility.distanceFrom(userLat: Double, userLng: Double): Float {
    val facilityLatLng = this.getLatLng() ?: return Float.MAX_VALUE
    val results = FloatArray(1)
    Location.distanceBetween(
        userLat, userLng,
        facilityLatLng.latitude, facilityLatLng.longitude,
        results
    )
    return results[0] // Khoảng cách tính bằng mét
}

class HealthMapViewModel(
    private val facilitiesRepository: FacilitiesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FacilitiesUiState>(FacilitiesUiState.Loading)
    val uiState: StateFlow<FacilitiesUiState> = _uiState.asStateFlow()

    // Lưu vị trí người dùng
    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val userLocation: StateFlow<Pair<Double, Double>?> = _userLocation.asStateFlow()

    // Danh sách gốc từ API
    private val _masterFacilityList = MutableStateFlow<List<Facility>>(emptyList())

    // Truy vấn tìm kiếm
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Bộ lọc loại cơ sở
    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType: StateFlow<String?> = _selectedType.asStateFlow()

    // Sắp xếp theo khoảng cách
    private val _sortByDistance = MutableStateFlow(false)
    val sortByDistance: StateFlow<Boolean> = _sortByDistance.asStateFlow()

    // Các hàm công khai để cập nhật trạng thái
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterType(type: String?) {
        _selectedType.value = type
    }

    fun setSortByDistance(enabled: Boolean) {
        _sortByDistance.value = enabled
    }

    fun setUserLocation(lat: Double, lng: Double) {
        _userLocation.value = Pair(lat, lng)
    }

    // StateFlow kết hợp tìm kiếm, lọc và sắp xếp
    val filteredFacilities: StateFlow<List<Facility>> = combine(
        _masterFacilityList,
        _searchQuery,
        _selectedType,
        _sortByDistance,
        _userLocation
    ) { facilities, query, type, sortDistance, location ->
        var filteredList = facilities

        // Áp dụng bộ lọc loại
        if (type != null) {
            filteredList = filteredList.filter { it.type.equals(type, ignoreCase = true) }
        }

        // Áp dụng tìm kiếm
        if (query.isNotBlank()) {
            filteredList = filteredList.filter {
                (it.name?.contains(query, ignoreCase = true) == true) ||
                        (it.address?.contains(query, ignoreCase = true) == true)
            }
        }

        // Sắp xếp theo khoảng cách nếu được bật
        if (sortDistance && location != null) {
            filteredList = filteredList.sortedBy {
                it.distanceFrom(location.first, location.second)
            }
        }

        filteredList
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun fetchNearestFacilities(lat: Double, lng: Double, radius: Int = 40000, limit: Int = 50) {
        _uiState.value = FacilitiesUiState.Loading
        setUserLocation(lat, lng)

        viewModelScope.launch {
            val result = facilitiesRepository.getNearestFacilities(
                lat = lat,
                lng = lng,
                radius = radius,
                limit = limit
            )

            result.onSuccess { facilities ->
                _masterFacilityList.value = facilities
                _uiState.value = FacilitiesUiState.Success(facilities)
            }.onFailure { throwable ->
                _uiState.value = FacilitiesUiState.Error(
                    throwable.message ?: "An unknown error occurred"
                )
            }
        }
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedType.value = null
        _sortByDistance.value = false
    }
}