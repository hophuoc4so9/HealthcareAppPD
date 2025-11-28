package com.example.healthcareapppd.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.utils.TokenManager
// ApiService và Patient nằm cùng package nên không cần import
// import com.example.healthcareapppd.presentation.ui.ApiService
// import com.example.healthcareapppd.presentation.ui.Patient

import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class PatientDetailFragment : Fragment() {

    private var patientId: String? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var appointmentsAdapter: PatientAppointmentsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        patientId = arguments?.getString("patient_id")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_patient_detail, container, false)

        // 1. Nút Back
        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            findNavController().navigateUp()
        }

        // 2. RecyclerView Lịch sử
        appointmentsAdapter = PatientAppointmentsAdapter()
        val rvAppointments = view.findViewById<RecyclerView>(R.id.rvPatientAppointments)
        rvAppointments.layoutManager = LinearLayoutManager(context)
        rvAppointments.adapter = appointmentsAdapter

        // 3. Gọi API (Kiểm tra null an toàn)
        if (patientId != null) {
            loadPatientDetail(view, patientId!!)
            loadPatientAppointments(patientId!!)
        } else {
            Toast.makeText(context, "Lỗi: Mất ID bệnh nhân", Toast.LENGTH_SHORT).show()
        }
        return view
    }

    private fun loadPatientDetail(view: View, id: String) {
        scope.launch {
            try {
                val token = TokenManager.getToken(requireContext()) ?: ""

                val response = withContext(Dispatchers.IO) {
                    ApiService.getPatientDetail(token, id)
                }

                if (response.success && response.data != null) {
                    bindData(view, response.data)
                } else {
                    Toast.makeText(context, "Lỗi: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun bindData(view: View, patient: Patient) {
        view.findViewById<TextView>(R.id.tvDetailName).text = patient.fullName
        view.findViewById<TextView>(R.id.tvDetailEmail).text = "Email: ${patient.email}"
        view.findViewById<TextView>(R.id.tvDetailPhone).text = "SĐT: ${patient.phoneNumber ?: "Chưa cập nhật"}"

        val genderStr = when(patient.sex?.lowercase()) {
            "male" -> "Nam"
            "female" -> "Nữ"
            else -> "Khác"
        }
        view.findViewById<TextView>(R.id.tvDetailGender).text = "Giới tính: $genderStr"

        // SỬA LỖI: patient.dateOfBirth là String?, hàm formatDate đã được sửa để nhận String?
        view.findViewById<TextView>(R.id.tvDetailDob).text = "Ngày sinh: ${formatDate(patient.dateOfBirth)}"

        view.findViewById<TextView>(R.id.tvDetailAddress).text = "Địa chỉ: ${patient.address ?: "Chưa cập nhật"}"

        view.findViewById<TextView>(R.id.tvDetailTotalAppointments).text = patient.totalAppointments ?: "0"
        view.findViewById<TextView>(R.id.tvDetailCompletedAppointments).text = patient.completedAppointments ?: "0"
        view.findViewById<TextView>(R.id.tvDetailUpcomingAppointments).text = patient.upcomingAppointments ?: "0"

        // SỬA LỖI: patient.lastAppointmentDate là String?
        view.findViewById<TextView>(R.id.tvDetailLastAppointmentDate).text =
            "Gần nhất: ${formatDate(patient.lastAppointmentDate)}"
    }

    private fun loadPatientAppointments(id: String) {
        scope.launch {
            try {
                val token = TokenManager.getToken(requireContext()) ?: ""
                val response = withContext(Dispatchers.IO) {
                    ApiService.getPatientAppointments(token, id)
                }
                if (response.success) {
                    appointmentsAdapter.submitList(response.data.appointments)
                }
            } catch (e: Exception) { }
        }
    }

    // --- FIX LỖI Ở ĐÂY: Thêm dấu ? vào tham số (isoDate: String?) ---
    private fun formatDate(isoDate: String?): String {
        if (isoDate.isNullOrEmpty()) return "Chưa cập nhật"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(isoDate)
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date!!)
        } catch (e: Exception) {
            try {
                val shortParser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = shortParser.parse(isoDate)
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date!!)
            } catch (ex: Exception) { isoDate }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

// Adapter
class PatientAppointmentsAdapter : RecyclerView.Adapter<PatientAppointmentsAdapter.ViewHolder>() {
    private var items: List<Appointment> = emptyList()

    fun submitList(list: List<Appointment>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_patient_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(app: Appointment) {
            // SỬA LỖI: app.slotStartTime là String?
            itemView.findViewById<TextView>(R.id.tvAppointmentDate).text = formatIsoToDate(app.slotStartTime)

            val tvStatus = itemView.findViewById<TextView>(R.id.tvAppointmentStatus)
            tvStatus.text = app.status

            if(app.status == "completed") {
                tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            } else if (app.status == "cancelled" || app.status.contains("cancelled")) {
                tvStatus.setTextColor(android.graphics.Color.parseColor("#F44336"))
            } else {
                tvStatus.setTextColor(android.graphics.Color.parseColor("#FF9800"))
            }

            itemView.findViewById<TextView>(R.id.tvAppointmentNotes).text = app.patientNotes ?: "Không có ghi chú"

            // SỬA LỖI: Các trường này là String?
            val timeStart = formatIsoToTime(app.slotStartTime)
            val timeEnd = formatIsoToTime(app.slotEndTime)
            itemView.findViewById<TextView>(R.id.tvAppointmentTime).text = "$timeStart - $timeEnd"
        }

        // --- FIX LỖI Ở ĐÂY: Thêm dấu ? vào tham số ---
        private fun formatIsoToDate(isoString: String?): String {
            if (isoString.isNullOrEmpty()) return ""
            return try {
                val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                input.timeZone = TimeZone.getTimeZone("UTC")
                val date = input.parse(isoString)
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date!!)
            } catch (e: Exception) { "" }
        }

        // --- FIX LỖI Ở ĐÂY: Thêm dấu ? vào tham số ---
        private fun formatIsoToTime(isoString: String?): String {
            if (isoString.isNullOrEmpty()) return "--:--"
            return try {
                val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                input.timeZone = TimeZone.getTimeZone("UTC")
                val date = input.parse(isoString)
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date!!)
            } catch (e: Exception) { "" }
        }
    }
}