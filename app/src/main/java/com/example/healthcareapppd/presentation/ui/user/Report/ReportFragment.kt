package com.example.healthcareapppd.presentation.ui.user.Report

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.healthcareapppd.databinding.FragmentReportBinding
import com.example.healthcareapppd.R
import com.example.healthcareapppd.utils.TokenManager
import com.example.healthcareapppd.data.health.generateMockHealthData
import com.example.healthcareapppd.data.health.HealthConnectManager
import com.example.healthcareapppd.data.health.HealthConnectSyncHelper
import com.example.healthcareapppd.domain.usecase.patient.GetLatestVitalsUseCase
import com.example.healthcareapppd.domain.usecase.patient.GetHealthMetricsUseCase
import com.example.healthcareapppd.domain.usecase.patient.AddHealthMetricsUseCase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var tokenManager: TokenManager
    private lateinit var healthConnectManager: HealthConnectManager
    private lateinit var syncHelper: HealthConnectSyncHelper
    
    private val getLatestVitalsUseCase = GetLatestVitalsUseCase()
    private val getHealthMetricsUseCase = GetHealthMetricsUseCase()
    private val addHealthMetricsUseCase = AddHealthMetricsUseCase()
    
    // Health Connect permissions launcher
    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Toast.makeText(context, "✅ Đã cấp quyền Health Connect", Toast.LENGTH_SHORT).show()
            syncHealthConnectData()
        } else {
            Toast.makeText(context, "❌ Cần cấp quyền để đọc dữ liệu sức khỏe", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext())
        healthConnectManager = HealthConnectManager(requireContext())
        syncHelper = HealthConnectSyncHelper(
            requireContext(),
            tokenManager,
            addHealthMetricsUseCase,
            healthConnectManager
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Hiển thị ngày hiện tại
        updateCurrentDate()

        binding.btnCalculateBmi.setOnClickListener {
            findNavController().navigate(R.id.action_reportFragment_to_bmiCalculatorFragment)
        }
        
        // Nút xem chi tiết - navigate to chart
        binding.btnViewAll.setOnClickListener {
            findNavController().navigate(R.id.action_reportFragment_to_healthChartFragment)
        }
        
        // Nút tạo mock data
        binding.btnGenerateMockData.setOnClickListener {
            generateMockData()
        }
        
        // Nút đồng bộ Health Connect
        binding.btnSyncHealthConnect.setOnClickListener {
            checkAndSyncHealthConnect()
        }
        
        // Load dữ liệu khi fragment được tạo
        loadLatestVitals()
        loadTodayMetrics()
    }
    
    /**
     * Kiểm tra và đồng bộ Health Connect
     */
    private fun checkAndSyncHealthConnect() {
        // Kiểm tra Health Connect có khả dụng không
        if (!healthConnectManager.isAvailable()) {
            Toast.makeText(
                context,
                "⚠️ Health Connect chưa cài đặt. Mở Play Store?",
                Toast.LENGTH_LONG
            ).show()
            healthConnectManager.installHealthConnect()
            return
        }
        
        // Request permissions
        lifecycleScope.launch {
            if (healthConnectManager.hasAllPermissions()) {
                // Đã có quyền, đồng bộ luôn
                syncHealthConnectData()
            } else {
                // Chưa có quyền, request
                requestPermissions.launch(
                    arrayOf(
                        "android.permission.health.READ_STEPS",
                        "android.permission.health.READ_DISTANCE",
                        "android.permission.health.READ_TOTAL_CALORIES_BURNED",
                        "android.permission.health.READ_SLEEP"
                    )
                )
            }
        }
    }
    
    /**
     * Đồng bộ dữ liệu từ Health Connect
     */
    private fun syncHealthConnectData() {
        binding.btnSyncHealthConnect.isEnabled = false
        binding.tvMockProgress.isVisible = true
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                syncHelper.syncTodayData { message ->
                    // Đảm bảo update UI trên main thread
                    viewLifecycleOwner.lifecycleScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                        binding.tvMockProgress.text = message
                    }
                }
                
                // Delay một chút để server kịp xử lý
                kotlinx.coroutines.delay(1000)
                
                // Reload dữ liệu
                loadTodayMetrics()
                
                Toast.makeText(context, "✅ Đã đồng bộ Health Connect!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                binding.tvMockProgress.text = "❌ Lỗi: ${e.message}"
                Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnSyncHealthConnect.isEnabled = true
            }
        }
    }
    
    /**
     * Tạo mock data cho 7 ngày
     */
    private fun generateMockData() {
        binding.btnGenerateMockData.isEnabled = false
        binding.tvMockProgress.isVisible = true
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                requireContext().generateMockHealthData(
                    onProgress = { message ->
                        binding.tvMockProgress.text = message
                    },
                    todayOnly = false
                )
                
                // Reload dữ liệu sau khi tạo xong
                loadLatestVitals()
                loadTodayMetrics()
                
                Toast.makeText(context, "✅ Đã tạo mock data thành công!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                binding.tvMockProgress.text = "❌ Lỗi: ${e.message}"
                Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnGenerateMockData.isEnabled = true
            }
        }
    }
    
    /**
     * Cập nhật ngày hiện tại
     */
    private fun updateCurrentDate() {
        val dateFormat = SimpleDateFormat("dd 'Tháng' MM, yyyy", Locale("vi"))
        binding.txtCurrentDate.text = dateFormat.format(Date())
    }
    
    private fun loadLatestVitals() {
        val token = tokenManager.getToken() ?: return
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        viewLifecycleOwner.lifecycleScope.launch {
            // Load quãng đường hôm nay
            val distanceResult = getHealthMetricsUseCase(token, "distance_meters", null, null)
            distanceResult.onSuccess { metrics ->
                Log.d("ReportFragment", "✅ Distance metrics count: ${metrics.size}")
                val todayMetrics = metrics.filter { 
                    it.startTime.startsWith(today) || it.endTime.startsWith(today)
                }
                val totalDistance = todayMetrics.sumOf { it.value.toDoubleOrNull() ?: 0.0 }
                // Hiển thị theo km nếu > 1000m, ngược lại hiển thị m
                _binding?.let { binding ->
                    if (totalDistance >= 1000) {
                        val km = totalDistance / 1000
                        binding.txtHeartRate.text = String.format("%.2f km", km)
                    } else {
                        binding.txtHeartRate.text = String.format("%.0f m", totalDistance)
                    }
                }
            }.onFailure {
                Log.e("ReportFragment", "❌ Failed to load distance: ${it.message}")
                _binding?.txtHeartRate?.text = "0 m"
            }
        }
    }
    
    private fun loadTodayMetrics() {
        val token = tokenManager.getToken() ?: return
        
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        Log.d("ReportFragment", "📊 Loading metrics for date: $today")
        
        viewLifecycleOwner.lifecycleScope.launch {
            // Load bước chân hôm nay - thử không filter date
            val stepsResult = getHealthMetricsUseCase(token, "steps", null, null)
            stepsResult.onSuccess { metrics ->
                Log.d("ReportFragment", "✅ Steps metrics count: ${metrics.size}")
                metrics.forEach { 
                    Log.d("ReportFragment", "   - Steps: ${it.value} (${it.startTime} to ${it.endTime})")
                }
                // Filter local theo date
                val todayMetrics = metrics.filter { 
                    it.startTime.startsWith(today) || it.endTime.startsWith(today)
                }
                Log.d("ReportFragment", "   - Today metrics: ${todayMetrics.size}")
                val totalSteps = todayMetrics.sumOf { it.value.toIntOrNull() ?: 0 }
                _binding?.txtSteps?.text = String.format("%,d", totalSteps)
            }.onFailure {
                Log.e("ReportFragment", "❌ Failed to load steps: ${it.message}")
                _binding?.txtSteps?.text = "0"
            }
            
            // Load calories hôm nay
            val caloriesResult = getHealthMetricsUseCase(token, "active_calories", null, null)
            caloriesResult.onSuccess { metrics ->
                Log.d("ReportFragment", "✅ Calories metrics count: ${metrics.size}")
                val todayMetrics = metrics.filter { 
                    it.startTime.startsWith(today) || it.endTime.startsWith(today)
                }
                val totalCalories = todayMetrics.sumOf { it.value.toIntOrNull() ?: 0 }
                _binding?.txtCalories?.text = String.format("%,d", totalCalories)
            }.onFailure {
                Log.e("ReportFragment", "❌ Failed to load calories: ${it.message}")
                _binding?.txtCalories?.text = "0"
            }
            
            // Load giấc ngủ
            val sleepResult = getHealthMetricsUseCase(token, "sleep_duration_minutes", null, null)
            sleepResult.onSuccess { metrics ->
                Log.d("ReportFragment", "✅ Sleep metrics count: ${metrics.size}")
                val todayMetrics = metrics.filter { 
                    it.startTime.startsWith(today) || it.endTime.startsWith(today)
                }
                val totalSleep = todayMetrics.sumOf { it.value.toIntOrNull() ?: 0 }
                val hours = totalSleep / 60
                val minutes = totalSleep % 60
                _binding?.txtSleep?.text = "${hours}h ${minutes}m"
            }.onFailure {
                Log.e("ReportFragment", "❌ Failed to load sleep: ${it.message}")
                _binding?.txtSleep?.text = "0h 0m"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}