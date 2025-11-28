package com.example.healthcareapppd.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.WelcomeActivity
import com.example.healthcareapppd.utils.TokenManager
// Import ApiService và Model cùng package
import com.example.healthcareapppd.presentation.ui.ApiService
import com.example.healthcareapppd.presentation.ui.Appointment
import com.example.healthcareapppd.presentation.ui.User

import kotlinx.coroutines.*

class AdminReportFragment : Fragment() {

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    // Khai báo Adapter
    private lateinit var usersAdapter: SimpleUserAdapter
    private lateinit var appointmentsAdapter: SimpleAppointmentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_dashboard_report, container, false)

        // 1. Xử lý Đăng xuất
        view.findViewById<LinearLayout>(R.id.menuLogout).setOnClickListener { logout() }

        // 2. Setup RecyclerView Users
        val rvUsers = view.findViewById<RecyclerView>(R.id.rvRecentUsers)
        rvUsers.layoutManager = LinearLayoutManager(context)
        usersAdapter = SimpleUserAdapter()
        rvUsers.adapter = usersAdapter

        // 3. Setup RecyclerView Appointments
        val rvAppointments = view.findViewById<RecyclerView>(R.id.rvRecentAppointments)
        rvAppointments.layoutManager = LinearLayoutManager(context)
        appointmentsAdapter = SimpleAppointmentAdapter()
        rvAppointments.adapter = appointmentsAdapter

        // 4. Load Data
        loadStats(view)

        return view
    }

    private fun loadStats(view: View) {
        scope.launch {
            try {
                val token = TokenManager.getToken(requireContext()) ?: return@launch

                // Gọi đúng hàm getAdminDashboardStats vừa tạo
                val response = withContext(Dispatchers.IO) {
                    ApiService.getAdminDashboardStats(token)
                }

                // Kiểm tra response.success và response.data
                if (response.success && response.data != null) {
                    val dashboardData = response.data // Đây là AdminDashboardData

                    // 1. Bind Stats (Số liệu)
                    val stats = dashboardData.stats
                    if (stats != null) {
                        view.findViewById<TextView>(R.id.tvTotalPatients).text = stats.totalPatients ?: "0"
                        view.findViewById<TextView>(R.id.tvTotalDoctors).text = stats.totalDoctors ?: "0"
                        view.findViewById<TextView>(R.id.tvTotalAppointments).text = stats.totalAppointments ?: "0"
                        view.findViewById<TextView>(R.id.tvPendingVerifications).text = stats.pendingVerifications ?: "0"

                        // Nếu layout có view này thì uncomment
                        // view.findViewById<TextView>(R.id.tvPublishedArticles).text = stats.publishedArticles ?: "0"
                    }

                    // 2. Bind Recent Users (Fix lỗi Cannot infer type)
                    dashboardData.recentUsers?.let { userList ->
                        usersAdapter.submitList(userList)
                    }

                    // 3. Bind Recent Appointments (Fix lỗi Cannot infer type)
                    dashboardData.recentAppointments?.let { apptList ->
                        appointmentsAdapter.submitList(apptList)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun logout() {
        val sharedPrefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()
        val intent = Intent(requireContext(), WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finishAffinity()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

// --- ADAPTERS ---

class SimpleUserAdapter : RecyclerView.Adapter<SimpleUserAdapter.ViewHolder>() {
    private var list = listOf<User>()
    fun submitList(l: List<User>) { list = l; notifyDataSetChanged() }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(v)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.itemView.findViewById<TextView>(android.R.id.text1).text = item.email
        holder.itemView.findViewById<TextView>(android.R.id.text2).text = "Role: ${item.role}"
    }
    override fun getItemCount() = list.size
}

class SimpleAppointmentAdapter : RecyclerView.Adapter<SimpleAppointmentAdapter.ViewHolder>() {
    private var list = listOf<Appointment>()
    fun submitList(l: List<Appointment>) { list = l; notifyDataSetChanged() }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(v)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        val docName = item.doctorName ?: "Unknown Doctor"
        val patName = item.patientName ?: "Unknown Patient"
        val time = item.startTime?.take(10) ?: "--"

        holder.itemView.findViewById<TextView>(android.R.id.text1).text = "$patName -> $docName"
        holder.itemView.findViewById<TextView>(android.R.id.text2).text = "${item.status} | $time"
    }
    override fun getItemCount() = list.size
}