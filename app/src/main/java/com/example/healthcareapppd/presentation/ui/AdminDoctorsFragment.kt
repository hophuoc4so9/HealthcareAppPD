package com.example.healthcareapppd.presentation.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log // Import Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.presentation.ui.ApiService
import com.example.healthcareapppd.presentation.ui.DoctorInfo
import com.example.healthcareapppd.utils.TokenManager
import kotlinx.coroutines.*

class AdminDoctorsFragment : Fragment() {

    private val TAG = "AdminDoctors" // Tag cho Logcat
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminDoctorsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_list, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewAdmin)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = AdminDoctorsAdapter(
            onApprove = { doctor -> showConfirmDialog(doctor, true) },
            onReject = { doctor -> showConfirmDialog(doctor, false) }
        )
        recyclerView.adapter = adapter

        loadPendingDoctors()
        return view
    }

    private fun loadPendingDoctors() {
        scope.launch {
            try {
                val token = TokenManager.getToken(requireContext())
                if (token == null) {
                    Log.e(TAG, "Token is null")
                    return@launch
                }

                // Log trước khi gọi API
                Log.d(TAG, "Fetching pending doctors...")

                val response = withContext(Dispatchers.IO) {
                    ApiService.getPendingDoctors(token)
                }

                // Log kết quả trả về
                Log.d(TAG, "Fetch Response: $response")

                if (response.success) {
                    val list = response.data.doctors
                    Log.d(TAG, "Loaded ${list.size} doctors")
                    adapter.submitList(list)

                    if (list.isEmpty()) {
                        // Toast.makeText(context, "Không có bác sĩ chờ duyệt", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "Fetch Error: ${response.message}")
                    Toast.makeText(context, "Lỗi tải dữ liệu: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fetch Exception: ${e.message}", e)
                Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showConfirmDialog(doctor: DoctorInfo, isApprove: Boolean) {
        val action = if (isApprove) "Chấp thuận" else "Từ chối"
        val message = "Bạn có chắc muốn $action bác sĩ ${doctor.full_name}?"

        AlertDialog.Builder(context)
            .setTitle("$action Bác sĩ")
            .setMessage(message)
            .setPositiveButton("Đồng ý") { _, _ ->
                verifyDoctor(doctor.user_id, isApprove)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun verifyDoctor(userId: String, isApprove: Boolean) {
        val status = if (isApprove) "approved" else "rejected"

        // Log hành động cập nhật
        Log.d(TAG, "Updating doctor $userId to status: $status")

        scope.launch {
            try {
                val token = TokenManager.getToken(requireContext()) ?: return@launch

                val response = withContext(Dispatchers.IO) {
                    ApiService.verifyDoctor(token, userId, status)
                }

                // Log kết quả cập nhật
                Log.d(TAG, "Update Response: $response")

                if (response.success) {
                    Toast.makeText(context, "Đã xử lý thành công", Toast.LENGTH_SHORT).show()
                    loadPendingDoctors() // Reload lại danh sách sau khi xử lý
                } else {
                    Log.e(TAG, "Update Error: ${response.message}")
                    Toast.makeText(context, "Lỗi: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Update Exception: ${e.message}", e)
                Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

// --- ADAPTER ---
class AdminDoctorsAdapter(
    private val onApprove: (DoctorInfo) -> Unit,
    private val onReject: (DoctorInfo) -> Unit
) : RecyclerView.Adapter<AdminDoctorsAdapter.ViewHolder>() {

    private var list = listOf<DoctorInfo>()

    fun submitList(l: List<DoctorInfo>) {
        list = l
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_doctor_verify, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.tvName.text = item.full_name
        holder.tvSpec.text = "Chuyên khoa: ${item.specialization}"
        holder.tvEmail.text = item.email

        holder.btnApprove.setOnClickListener { onApprove(item) }
        holder.btnReject.setOnClickListener { onReject(item) }
    }

    override fun getItemCount() = list.size

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvDoctorName)
        val tvSpec: TextView = v.findViewById(R.id.tvDoctorSpec)
        val tvEmail: TextView = v.findViewById(R.id.tvDoctorEmail)
        val btnApprove: Button = v.findViewById(R.id.btnApprove)
        val btnReject: Button = v.findViewById(R.id.btnReject)
    }
}