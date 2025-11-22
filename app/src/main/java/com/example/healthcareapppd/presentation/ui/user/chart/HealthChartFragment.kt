package com.example.healthcareapppd.presentation.ui.user.chart

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.healthcareapppd.databinding.FragmentHealthChartBinding
import com.example.healthcareapppd.domain.usecase.patient.GetHealthMetricsUseCase
import com.example.healthcareapppd.utils.TokenManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HealthChartFragment : Fragment() {

    private var _binding: FragmentHealthChartBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var tokenManager: TokenManager
    private val getHealthMetricsUseCase = GetHealthMetricsUseCase()
    
    private var currentMetricType = "steps"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthChartBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupChipListeners()
        loadChartData("steps")
    }
    
    private fun setupChipListeners() {
        binding.chipSteps.setOnClickListener {
            currentMetricType = "steps"
            loadChartData("steps")
        }
        
        binding.chipCalories.setOnClickListener {
            currentMetricType = "active_calories"
            loadChartData("active_calories")
        }
        
        binding.chipDistance.setOnClickListener {
            currentMetricType = "distance_meters"
            loadChartData("distance_meters")
        }
        
        binding.chipSleep.setOnClickListener {
            currentMetricType = "sleep_duration_minutes"
            loadChartData("sleep_duration_minutes")
        }
    }
    
    private fun loadChartData(metricType: String) {
        val token = tokenManager.getToken() ?: return
        
        binding.progressBar.visibility = View.VISIBLE
        binding.lineChart.visibility = View.GONE
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Lấy dữ liệu 7 ngày gần nhất
                val result = getHealthMetricsUseCase(token, metricType, null, null)
                
                result.onSuccess { metrics ->
                    Log.d("HealthChartFragment", "✅ Loaded ${metrics.size} metrics for $metricType")
                    
                    // Nhóm theo ngày và tính tổng
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val displayFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                    
                    val dailyData = metrics
                        .groupBy { metric ->
                            metric.startTime.substring(0, 10) // Extract yyyy-MM-dd
                        }
                        .mapValues { (_, dayMetrics) ->
                            dayMetrics.sumOf { metric -> metric.value.toDoubleOrNull() ?: 0.0 }
                        }
                        .toSortedMap() // Sort by date
                        .toList()
                        .takeLast(7) // Lấy 7 ngày gần nhất
                        .toMap()
                    
                    if (dailyData.isEmpty()) {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, "Chưa có dữ liệu cho chỉ số này", Toast.LENGTH_SHORT).show()
                        return@onSuccess
                    }
                    
                    // Tạo entries cho biểu đồ
                    val entries = dailyData.entries.mapIndexed { index, (date, value) ->
                        Entry(index.toFloat(), value.toFloat())
                    }
                    
                    // Tạo labels cho trục X
                    val labels = dailyData.keys.map { date ->
                        try {
                            val parsedDate = dateFormat.parse(date)
                            displayFormat.format(parsedDate!!)
                        } catch (e: Exception) {
                            date
                        }
                    }
                    
                    // Hiển thị biểu đồ
                    displayLineChart(entries, labels, metricType)
                    
                    binding.progressBar.visibility = View.GONE
                    binding.lineChart.visibility = View.VISIBLE
                }.onFailure { error ->
                    Log.e("HealthChartFragment", "❌ Failed to load metrics: ${error.message}")
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("HealthChartFragment", "❌ Exception: ${e.message}")
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun displayLineChart(entries: List<Entry>, labels: List<String>, metricType: String) {
        val dataSet = LineDataSet(entries, getChartLabel(metricType)).apply {
            color = getChartColor(metricType)
            setCircleColor(getChartColor(metricType))
            lineWidth = 3f
            circleRadius = 6f
            setDrawCircleHole(false)
            valueTextSize = 12f
            valueTextColor = Color.BLACK
            setDrawFilled(true)
            fillColor = getChartColor(metricType)
            fillAlpha = 50
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        
        val lineData = LineData(dataSet)
        binding.lineChart.apply {
            data = lineData
            description.isEnabled = false
            legend.textSize = 14f
            
            // Cấu hình trục X
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textSize = 12f
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return labels.getOrNull(value.toInt()) ?: ""
                    }
                }
            }
            
            // Cấu hình trục Y bên trái
            axisLeft.apply {
                textSize = 12f
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
            }
            
            // Ẩn trục Y bên phải
            axisRight.isEnabled = false
            
            // Animation
            animateX(500)
            
            invalidate() // Refresh
        }
    }
    
    private fun getChartLabel(metricType: String): String {
        return when (metricType) {
            "steps" -> "Bước chân"
            "active_calories" -> "Calories (kcal)"
            "distance_meters" -> "Quãng đường (m)"
            "sleep_duration_minutes" -> "Giấc ngủ (phút)"
            else -> metricType
        }
    }
    
    private fun getChartColor(metricType: String): Int {
        return when (metricType) {
            "steps" -> Color.parseColor("#4CAF50") // Green
            "active_calories" -> Color.parseColor("#FF9800") // Orange
            "distance_meters" -> Color.parseColor("#2196F3") // Blue
            "sleep_duration_minutes" -> Color.parseColor("#9C27B0") // Purple
            else -> Color.parseColor("#6C63FF")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
