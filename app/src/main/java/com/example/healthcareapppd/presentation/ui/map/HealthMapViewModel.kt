package com.example.healthcareapppd.presentation.ui.map

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthcareapppd.data.api.getLatLng
import com.example.healthcareapppd.data.api.model.Facility
import com.example.healthcareapppd.domain.repository.FacilitiesRepository
import com.example.healthcareapppd.utils.SemanticSearchEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.regex.Pattern
import kotlin.math.max

// Hàm mở rộng để bỏ dấu tiếng Việt (Bình Dương -> binh duong)
fun String.unaccent(): String {
    val nfdNormalizedString = Normalizer.normalize(this, Normalizer.Form.NFD)
    val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    return pattern.matcher(nfdNormalizedString).replaceAll("").lowercase().trim()
}

sealed class FacilitiesUiState {
    object Loading : FacilitiesUiState()
    data class Success(val facilities: List<Facility>) : FacilitiesUiState()
    data class Error(val message: String) : FacilitiesUiState()
}

fun Facility.distanceFrom(userLat: Double, userLng: Double): Float {
    val facilityLatLng = this.getLatLng() ?: return Float.MAX_VALUE
    val results = FloatArray(1)
    Location.distanceBetween(
        userLat, userLng,
        facilityLatLng.latitude, facilityLatLng.longitude,
        results
    )
    return results[0]
}

class HealthMapViewModel(
    private val facilitiesRepository: FacilitiesRepository,
    private val searchEngine: SemanticSearchEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow<FacilitiesUiState>(FacilitiesUiState.Loading)
    val uiState: StateFlow<FacilitiesUiState> = _uiState.asStateFlow()

    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val userLocation: StateFlow<Pair<Double, Double>?> = _userLocation.asStateFlow()

    private val _masterFacilityList = MutableStateFlow<List<Facility>>(emptyList())

    // Cache Vector: ID -> Vector
    private val facilityVectorCache = mutableMapOf<String, FloatArray>()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType: StateFlow<String?> = _selectedType.asStateFlow()

    private val _sortByDistance = MutableStateFlow(false)
    val sortByDistance: StateFlow<Boolean> = _sortByDistance.asStateFlow()

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

    // --- CORE LOGIC: TÌM KIẾM & LỌC ---
    val filteredFacilities: StateFlow<List<Facility>> = combine(
        _masterFacilityList,
        _searchQuery,
        _selectedType,
        _sortByDistance,
        _userLocation
    ) { facilities, query, type, sortDistance, location ->
        var filteredList = facilities

        // 1. Lọc theo Loại (Type)
        if (type != null) {
            filteredList = filteredList.filter { it.type.equals(type, ignoreCase = true) }
        }

        // 2. Tìm kiếm thông minh (Hybrid: Keyword + AI)
        if (query.isNotBlank()) {
            // Encode query sang vector
            val queryVector = searchEngine.encode(query)

            // Debug Log nếu Vector lỗi
            if (queryVector == null) Log.e("SearchDebug", "⚠️ Query Vector NULL")

            // Chuẩn hóa query để tìm chính xác (binh duong)
            val queryNormalized = query.unaccent()

            filteredList = filteredList.mapNotNull { facility ->
                // --- A. Text Match (Tuyệt đối) ---
                val nameNorm = facility.name?.unaccent() ?: ""
                val addressNorm = facility.address?.unaccent() ?: ""
                val isTextMatch = nameNorm.contains(queryNormalized) || addressNorm.contains(queryNormalized)
                val textScore = if (isTextMatch) 1.0f else 0.0f

                // --- B. AI Match (Tương đối) ---
                val facilityId = facility.id.toString()
                val facilityVector = facilityVectorCache[facilityId]
                var aiScore = 0.0f

                if (queryVector != null && facilityVector != null) {
                    aiScore = searchEngine.cosineSimilarity(queryVector, facilityVector)
                }

                // --- C. Debugging ---
                // In log để bạn kiểm tra tại sao kết quả hiện/ẩn
                if (textScore > 0 || aiScore > 0.15) {
                    Log.d("SearchDebug", "Facility: ${facility.name} | Text: $textScore | AI: $aiScore")
                }

                // --- D. Quyết định ---
                // Lấy điểm cao nhất. Ngưỡng AI là 0.25
                val finalScore = max(textScore, aiScore)
                if (finalScore > 0.25f) Pair(facility, finalScore) else null
            }
                .sortedByDescending { it.second } // Xếp theo điểm số
                .map { it.first }
        }

        // 3. Sắp xếp khoảng cách
        if (sortDistance && location != null) {
            filteredList = filteredList.sortedBy {
                it.distanceFrom(location.first, location.second)
            }
        }

        filteredList
    }
        .flowOn(Dispatchers.Default) // Chạy trên background thread
        .stateIn(
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

                // [QUAN TRỌNG] Tạo vector ngay khi có dữ liệu
                prepareFacilityVectors(facilities)

            }.onFailure { throwable ->
                _uiState.value = FacilitiesUiState.Error(
                    throwable.message ?: "An unknown error occurred"
                )
            }
        }
    }

    private fun prepareFacilityVectors(facilities: List<Facility>) {
        Log.d("SearchDebug", "🔄 Bắt đầu tạo Vector cho ${facilities.size} cơ sở...")
        viewModelScope.launch(Dispatchers.Default) {
            facilities.forEach { facility ->
                // Kết hợp Tên + Loại + ĐỊA CHỈ để AI hiểu ngữ cảnh
                val textToEmbed = "${facility.name ?: ""} ${facility.type ?: ""} ${facility.address ?: ""}"

                val vector = searchEngine.encode(textToEmbed)
                if (vector != null) {
                    facilityVectorCache[facility.id.toString()] = vector
                }
            }
            Log.d("SearchDebug", "✅ Đã tạo xong Vector Cache. Size: ${facilityVectorCache.size}")
        }
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedType.value = null
        _sortByDistance.value = false
    }

    override fun onCleared() {
        super.onCleared()
        searchEngine.close()
    }
}