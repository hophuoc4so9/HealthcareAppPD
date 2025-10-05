package com.example.healthcareapppd.presentation.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.data.api.Facility
import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.getLatLng
import com.example.healthcareapppd.domain.repository.FacilitiesRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var facilityAdapter: FacilityAdapter
    private lateinit var facilitiesRecyclerView: RecyclerView

    // UI Components
    private lateinit var searchEditText: EditText
    private lateinit var filterButton: ImageButton
    private lateinit var chipGroup: ChipGroup
    private lateinit var chipSortDistance: Chip
    private lateinit var chipHospital: Chip
    private lateinit var chipClinic: Chip
    private lateinit var chipPharmacy: Chip
    private lateinit var chipDentist: Chip
    private lateinit var resultsCountText: TextView
    private lateinit var clearFiltersButton: ImageButton
    private lateinit var progressBar: ProgressBar

    private val viewModel: HealthMapViewModel by viewModels {
        HealthMapViewModelFactory(
            FacilitiesRepository(RetrofitClient.instance)
        )
    }

    private val markers = mutableMapOf<Int, Marker?>()

    private val locationPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                getDeviceLocation()
            } else {
                Toast.makeText(requireContext(), "Quyền truy cập vị trí bị từ chối", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupRecyclerView(view)
        setupSearchBar()
        setupChips()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        observeViewModel()
    }

    private fun initializeViews(view: View) {
        searchEditText = view.findViewById(R.id.search_edit_text)
        filterButton = view.findViewById(R.id.filter_button)
        chipGroup = view.findViewById(R.id.chip_group_filters)
        chipSortDistance = view.findViewById(R.id.chip_sort_distance)
        chipHospital = view.findViewById(R.id.chip_hospital)
        chipClinic = view.findViewById(R.id.chip_clinic)
        chipPharmacy = view.findViewById(R.id.chip_pharmacy)
        chipDentist = view.findViewById(R.id.chip_dentist)
        resultsCountText = view.findViewById(R.id.results_count_text)
        clearFiltersButton = view.findViewById(R.id.clear_filters_button)
        progressBar = view.findViewById(R.id.progress_bar)
    }

    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map
        checkLocationPermission()
        setupMapListeners()
    }

    private fun setupRecyclerView(view: View) {
        facilityAdapter = FacilityAdapter(
            onItemClicked = { facility ->
                // Click thường - zoom vào marker trên bản đồ
                val latLng = facility.getLatLng()
                latLng?.let {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
                    markers[facility.id]?.showInfoWindow()
                }
            },
            onItemLongClicked = { facility ->
                // Long click - điều hướng bằng Google Maps
                NavigationHelper.startTurnByTurnNavigation(requireContext(), facility)
            }
        )
        facilitiesRecyclerView = view.findViewById(R.id.facilities_recycler_view)
        facilitiesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        facilitiesRecyclerView.adapter = facilityAdapter
    }

    private fun setupSearchBar() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
        })

        filterButton.setOnClickListener {
            // TODO: Mở bottom sheet filter nâng cao
            Toast.makeText(requireContext(), "Bộ lọc nâng cao", Toast.LENGTH_SHORT).show()
        }

        clearFiltersButton.setOnClickListener {
            viewModel.clearFilters()
            searchEditText.text.clear()
            chipGroup.clearCheck()
        }
    }

    private fun setupChips() {
        // Chip sắp xếp theo khoảng cách
        chipSortDistance.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setSortByDistance(isChecked)
        }

        // Chip lọc theo loại
        chipHospital.setOnCheckedChangeListener { _, isChecked ->
            updateTypeFilter(isChecked, "hospital")
        }

        chipClinic.setOnCheckedChangeListener { _, isChecked ->
            updateTypeFilter(isChecked, "clinic")
        }

        chipPharmacy.setOnCheckedChangeListener { _, isChecked ->
            updateTypeFilter(isChecked, "pharmacy")
        }

        chipDentist.setOnCheckedChangeListener { _, isChecked ->
            updateTypeFilter(isChecked, "dentist")
        }
    }

    private fun updateTypeFilter(isChecked: Boolean, type: String) {
        if (isChecked) {
            // Bỏ chọn các chip khác (single selection cho type)
            when (type) {
                "hospital" -> {
                    chipClinic.isChecked = false
                    chipPharmacy.isChecked = false
                    chipDentist.isChecked = false
                }
                "clinic" -> {
                    chipHospital.isChecked = false
                    chipPharmacy.isChecked = false
                    chipDentist.isChecked = false
                }
                "pharmacy" -> {
                    chipHospital.isChecked = false
                    chipClinic.isChecked = false
                    chipDentist.isChecked = false
                }
                "dentist" -> {
                    chipHospital.isChecked = false
                    chipClinic.isChecked = false
                    chipPharmacy.isChecked = false
                }
            }
            viewModel.setFilterType(type)
        } else {
            viewModel.setFilterType(null)
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun setupMapListeners() {
        // Click vào info window (chữ trên marker) để điều hướng
        googleMap.setOnInfoWindowClickListener { marker ->
            val facility = marker.tag as? Facility
            facility?.let {
                NavigationHelper.startTurnByTurnNavigation(requireContext(), it)
            }
        }

        // Click vào marker để scroll đến item trong list
        googleMap.setOnMarkerClickListener { marker ->
            val facility = marker.tag as? Facility
            facility?.let {
                val position = facilityAdapter.currentList.indexOf(it)
                if (position != -1) {
                    facilitiesRecyclerView.smoothScrollToPosition(position)
                }
            }
            false
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is FacilitiesUiState.Loading -> {
                            progressBar.visibility = View.VISIBLE
                        }
                        is FacilitiesUiState.Success -> {
                            progressBar.visibility = View.GONE
                        }
                        is FacilitiesUiState.Error -> {
                            progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), "Lỗi: ${state.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredFacilities.collect { facilities ->
                    if (::googleMap.isInitialized) {
                        updateMapMarkers(facilities)
                    }
                    facilityAdapter.submitList(facilities)

                    // Cập nhật số lượng kết quả
                    resultsCountText.text = "Tìm thấy ${facilities.size} kết quả"

                    // Hiển thị nút xóa filter nếu có filter đang hoạt động
                    clearFiltersButton.visibility = if (
                        viewModel.searchQuery.value.isNotBlank() ||
                        viewModel.selectedType.value != null ||
                        viewModel.sortByDistance.value
                    ) View.VISIBLE else View.GONE
                }
            }
        }

        // Observe user location để cập nhật adapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userLocation.collect { location ->
                    location?.let {
                        facilityAdapter.setUserLocation(it.first, it.second)
                    }
                }
            }
        }
    }

    private fun updateMapMarkers(facilities: List<Facility>) {
        googleMap.clear()
        markers.clear()

        facilities.forEach { facility ->
            val latLng = facility.getLatLng()
            latLng?.let {
                val markerOptions = MarkerOptions()
                    .position(it)
                    .title(facility.name ?: "Không có tên")
                    .snippet(facility.type)

                val marker = googleMap.addMarker(markerOptions)
                marker?.tag = facility
                if (marker != null) {
                    markers[facility.id] = marker
                }
            }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getDeviceLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        googleMap.isMyLocationEnabled = true
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                    viewModel.fetchNearestFacilities(location.latitude, location.longitude)
                } else {
                    Toast.makeText(requireContext(), "Không thể lấy vị trí. Vui lòng bật dịch vụ vị trí.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}