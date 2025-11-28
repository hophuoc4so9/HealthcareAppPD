package com.example.healthcareapppd.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.healthcareapppd.R
import kotlinx.coroutines.*

class DoctorReportFragment : Fragment() {
    
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var tvTotalPatients: TextView
    private lateinit var tvTotalAppointments: TextView
    private lateinit var tvUpcomingAppointments: TextView
    private lateinit var tvCompletedAppointments: TextView
    private lateinit var tvTodayAppointments: TextView
    private lateinit var tvPendingAppointments: TextView
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_doctor_report, container, false)
        
        initViews(view)
        setupSwipeRefresh()
        loadDashboardStats()
        
        return view
    }
    
    private fun initViews(view: View) {
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        tvTotalPatients = view.findViewById(R.id.tvTotalPatients)
        tvTotalAppointments = view.findViewById(R.id.tvTotalAppointments)
        tvUpcomingAppointments = view.findViewById(R.id.tvUpcomingAppointments)
        tvCompletedAppointments = view.findViewById(R.id.tvCompletedAppointments)
        tvTodayAppointments = view.findViewById(R.id.tvTodayAppointments)
        tvPendingAppointments = view.findViewById(R.id.tvPendingAppointments)
    }
    
    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            loadDashboardStats()
        }
    }
    
    private fun loadDashboardStats() {
        swipeRefresh.isRefreshing = true

        scope.launch {
            try {
                val token = getAuthToken()
                android.util.Log.d("DoctorReportFragment", "Token used for API: $token")
                val response = withContext(Dispatchers.IO) {
                    ApiService.getDoctorDashboardStats(token)
                }
                android.util.Log.d("DoctorReportFragment", "API response: $response")
                if (response.success) {
                    updateUI(response.data)
                } else {
                    android.util.Log.e("DoctorReportFragment", "API error: ${response.message}")
                    Toast.makeText(context, "Lỗi: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("DoctorReportFragment", "Exception: ${e.message}", e)
                Toast.makeText(context, "Không thể tải thống kê: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }
    
    private fun updateUI(stats: DashboardStats) {
        tvTotalPatients.text = stats.totalPatients.toString()
        tvTotalAppointments.text = stats.totalAppointments.toString()
        tvUpcomingAppointments.text = stats.upcomingAppointments.toString()
        tvCompletedAppointments.text = stats.completedAppointments.toString()
        tvTodayAppointments.text = stats.todayAppointments.toString()
        tvPendingAppointments.text = stats.pendingAppointments.toString()
    }
    
    private fun getAuthToken(): String {
        val tokenFromManager = com.example.healthcareapppd.utils.TokenManager.getToken(requireContext())
        if (!tokenFromManager.isNullOrEmpty()) {
            android.util.Log.d("DoctorReportFragment", "TokenManager token: $tokenFromManager")
            return tokenFromManager
        }
        val sharedPrefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val token = sharedPrefs.getString("doctor_token", "") ?: ""
        android.util.Log.d("DoctorReportFragment", "SharedPrefs token: $token")
        if (token.isEmpty()) {
            android.util.Log.e("DoctorReportFragment", "Token is empty! API will fail.")
        }
        return token
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}