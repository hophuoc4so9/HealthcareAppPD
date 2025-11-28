package com.example.healthcareapppd.presentation.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.healthcareapppd.R
import com.example.healthcareapppd.presentation.ui.ApiService

// Thêm thư viện để format ngày giờ
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class DoctorAppointmentsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chipGroup: ChipGroup
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var adapter: AppointmentsAdapter
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var currentStatus: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_doctor_appointments, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewAppointments)
        chipGroup = view.findViewById(R.id.chipGroupStatus)
        swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)

        setupRecyclerView()
        setupChipGroup()
        setupSwipeRefresh()

        loadAppointments()

        return view
    }

    private fun setupRecyclerView() {
        adapter = AppointmentsAdapter(
            onItemClick = { appointment ->
                // Hiển thị ghi chú khi click vào
                val note = appointment.patientNotes ?: "Không có ghi chú"
                Toast.makeText(context, "Ghi chú: $note", Toast.LENGTH_SHORT).show()
            },
            onItemLongClick = { appointment ->
                showUpdateStatusDialog(appointment)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun showUpdateStatusDialog(appointment: Appointment) {
        if (appointment.status == "pending") { // Lưu ý: JSON của bạn ghi "scheduled" hay "pending"? Hãy check kỹ
            // Nếu JSON trả về "scheduled", hãy đổi dòng trên thành: if (appointment.status == "scheduled")
            val options = arrayOf("Đánh dấu Hoàn thành", "Hủy lịch hẹn")
            val values = arrayOf("completed", "cancelled")

            AlertDialog.Builder(requireContext())
                .setTitle("Xử lý lịch hẹn?")
                .setItems(options) { _, which ->
                    updateAppointmentStatus(appointment.id, values[which])
                }
                .setNegativeButton("Đóng", null)
                .show()
        } else if (appointment.status == "scheduled") { // Xử lý thêm trường hợp scheduled
            val options = arrayOf("Hoàn thành", "Hủy")
            val values = arrayOf("completed", "cancelled")
            AlertDialog.Builder(requireContext())
                .setTitle("Xử lý lịch hẹn?")
                .setItems(options) { _, which ->
                    updateAppointmentStatus(appointment.id, values[which])
                }
                .setNegativeButton("Đóng", null)
                .show()
        } else {
            Toast.makeText(context, "Lịch hẹn đã kết thúc.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAppointmentStatus(appointmentId: String, newStatus: String) {
        swipeRefresh.isRefreshing = true
        scope.launch {
            try {
                val token = getAuthToken()
                if (token.isEmpty()) return@launch

                val response = withContext(Dispatchers.IO) {
                    ApiService.updateAppointmentStatus(token, appointmentId, newStatus)
                }

                if (response.success) {
                    Toast.makeText(context, "Thành công!", Toast.LENGTH_SHORT).show()
                    loadAppointments()
                } else {
                    Toast.makeText(context, "Lỗi: ${response.message}", Toast.LENGTH_SHORT).show()
                    swipeRefresh.isRefreshing = false
                }
            } catch (e: Exception) {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun setupChipGroup() {
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) {
                currentStatus = null
            } else {
                val selectedId = checkedIds[0]
                currentStatus = when (selectedId) {
                    R.id.chipPending -> "pending" // Hoặc "scheduled" tùy API của bạn
                    R.id.chipCompleted -> "completed"
                    R.id.chipAll -> null
                    else -> null
                }
            }
            loadAppointments()
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener { loadAppointments() }
    }

    private fun loadAppointments() {
        swipeRefresh.isRefreshing = true
        scope.launch {
            try {
                val token = getAuthToken()
                val response = withContext(Dispatchers.IO) {
                    ApiService.getDoctorAppointments(token, currentStatus)
                }
                if (response.success) {
                    // API trả về danh sách trong response.data.appointments
                    adapter.submitList(response.data.appointments)
                }
            } catch (e: Exception) {
                // Ignore
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun getAuthToken(): String {
        val token = com.example.healthcareapppd.utils.TokenManager.getToken(requireContext())
        if (!token.isNullOrEmpty()) return token
        return requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            .getString("doctor_token", "") ?: ""
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

// --- ADAPTER CẬP NHẬT ---
class AppointmentsAdapter(
    private val onItemClick: (Appointment) -> Unit,
    private val onItemLongClick: (Appointment) -> Unit
) : androidx.recyclerview.widget.ListAdapter<Appointment, AppointmentsAdapter.ViewHolder>(
    AppointmentDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_doctor_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick, onItemLongClick)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPatientName: android.widget.TextView = itemView.findViewById(R.id.tvPatientName)
        private val tvDateTime: android.widget.TextView = itemView.findViewById(R.id.tvDateTime)
        private val tvStatus: android.widget.TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(
            appointment: Appointment,
            onClick: (Appointment) -> Unit,
            onLongClick: (Appointment) -> Unit
        ) {
            tvPatientName.text = appointment.patientName

            // XỬ LÝ NGÀY GIỜ TỪ ISO STRING
            // Input: "2025-11-26T10:00:00.000Z" -> Output: "26/11/2025 | 10:00"
            val formattedTime = formatIsoDateTime(appointment.slotStartTime)
            tvDateTime.text = formattedTime

            // XỬ LÝ TRẠNG THÁI
            when(appointment.status) {
                "scheduled", "pending" -> { // Xử lý cả 2 trường hợp tên
                    tvStatus.text = "Đã đặt (Chờ)"
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#FF9800")) // Cam
                }
                "completed" -> {
                    tvStatus.text = "Hoàn thành"
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50")) // Xanh lá
                }
                "cancelled" -> {
                    tvStatus.text = "Đã hủy"
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#F44336")) // Đỏ
                }
                else -> {
                    tvStatus.text = appointment.status
                    tvStatus.setTextColor(android.graphics.Color.GRAY)
                }
            }

            itemView.setOnClickListener { onClick(appointment) }
            itemView.setOnLongClickListener {
                onLongClick(appointment)
                true
            }
        }

        // Hàm helper để format ngày giờ
        private fun formatIsoDateTime(isoString: String?): String {
            // Kiểm tra null hoặc rỗng
            if (isoString.isNullOrEmpty()) return "--:--"

            return try {
                // Định dạng đầu vào (ISO 8601 từ server)
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(isoString) ?: return isoString

                // Định dạng đầu ra
                val outputDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val outputTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                "${outputDateFormat.format(date)} | ${outputTimeFormat.format(date)}"
            } catch (e: Exception) {
                isoString // Trả về chuỗi gốc nếu lỗi
            }
        }
    }
}

class AppointmentDiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<Appointment>() {
    override fun areItemsTheSame(oldItem: Appointment, newItem: Appointment): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Appointment, newItem: Appointment): Boolean = oldItem == newItem
}