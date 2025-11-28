package com.example.healthcareapppd.presentation.ui

import android.os.Bundle
import android.util.Log // Import Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.healthcareapppd.R
import com.example.healthcareapppd.utils.TokenManager
// Import ApiService và Patient nằm cùng package (vì bạn đã để chung)
// Nếu IDE báo lỗi, hãy xóa dòng import bên dưới, nó sẽ tự nhận diện class trong cùng package
// import com.example.healthcareapppd.presentation.ui.ApiService
// import com.example.healthcareapppd.presentation.ui.Patient

import kotlinx.coroutines.*

class DoctorPatientsFragment : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var adapter: PatientsAdapter

    // Tạo scope gắn với lifecycle của Fragment để tránh memory leak
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_doctor_patients, container, false)

        // Ánh xạ View
        searchView = view.findViewById(R.id.searchView)
        recyclerView = view.findViewById(R.id.recyclerViewPatients)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)

        setupRecyclerView()
        setupSearchView()
        setupSwipeRefresh()

        // Gọi tải dữ liệu ngay khi màn hình tạo xong
        loadPatients()

        return view
    }

    private fun setupRecyclerView() {
        adapter = PatientsAdapter { patient ->
            navigateToPatientDetail(patient)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun navigateToPatientDetail(patient: Patient) {
        val bundle = Bundle().apply {
            putString("patient_id", patient.id)
        }
        try {
            // SỬA: Dùng ID action mới nằm trong DoctorHomeFragment
            findNavController().navigate(R.id.action_doctorHome_to_patientDetailFragment, bundle)

            // HOẶC: Cách nhanh nhất (nếu lười sửa XML) là điều hướng thẳng tới ID đích:
            // findNavController().navigate(R.id.patientDetailFragment, bundle)
        } catch (e: Exception) {
            e.printStackTrace()
            // In log để debug
            android.util.Log.e("NavError", "Lỗi điều hướng: ${e.message}")
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus() // Ẩn bàn phím khi ấn enter
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return true
            }
        })
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            loadPatients()
        }
    }

    // --- LOGIC LOAD DATA CHÍNH ---
    private fun loadPatients() {
        swipeRefresh.isRefreshing = true

        scope.launch {
            try {
                // 1. Lấy Token
                val token = TokenManager.getToken(requireContext())

                if (token.isNullOrEmpty()) {
                    Toast.makeText(context, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show()
                    swipeRefresh.isRefreshing = false
                    return@launch
                }

                // 2. Gọi API trên luồng IO
                val response = withContext(Dispatchers.IO) {
                    // Gọi hàm static trong Object ApiService
                    ApiService.getDoctorPatients(token, limit = 100)
                }

                // 3. Cập nhật UI trên luồng Main
                if (response.success) {
                    val patients = response.data ?: emptyList()
                    adapter.submitList(patients)
                    if (patients.isEmpty()) {
                        // Toast.makeText(context, "Không có bệnh nhân nào", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Lỗi tải dữ liệu: ${response.message}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("DoctorPatients", "Error loading patients", e)
                Toast.makeText(context, "Không thể kết nối đến máy chủ", Toast.LENGTH_SHORT).show()
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel() // Hủy coroutine khi thoát màn hình
    }
}

// --- ADAPTER ---
class PatientsAdapter(
    private val onItemClick: (Patient) -> Unit
) : ListAdapter<Patient, PatientsAdapter.ViewHolder>(PatientDiffCallback()) {

    private var originalList = listOf<Patient>()

    // Override submitList để lưu lại danh sách gốc phục vụ việc search
    override fun submitList(list: List<Patient>?) {
        val safeList = list ?: emptyList()
        originalList = safeList
        super.submitList(safeList)
    }

    fun filter(query: String) {
        val filtered = if (query.isEmpty()) {
            originalList
        } else {
            originalList.filter {
                (it.fullName.contains(query, ignoreCase = true)) ||
                        (it.email.contains(query, ignoreCase = true)) ||
                        (it.phoneNumber?.contains(query, ignoreCase = true) == true)
            }
        }
        // Gọi super.submitList để update giao diện, KHÔNG lưu vào originalList
        super.submitList(filtered)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPatientName: TextView = itemView.findViewById(R.id.tvPatientName)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        private val tvPhone: TextView = itemView.findViewById(R.id.tvPhone)
        private val tvAppointments: TextView = itemView.findViewById(R.id.tvAppointments)

        fun bind(patient: Patient, onClick: (Patient) -> Unit) {
            tvPatientName.text = patient.fullName
            tvEmail.text = patient.email
            tvPhone.text = patient.phoneNumber ?: "Chưa cập nhật SĐT"

            // Format hiển thị lịch hẹn (Vì totalAppointments là String nên dùng trực tiếp)
            val total = patient.totalAppointments ?: "0"
            val completed = patient.completedAppointments ?: "0"
            tvAppointments.text = "Tổng: $total | Xong: $completed"

            itemView.setOnClickListener { onClick(patient) }
        }
    }
}

class PatientDiffCallback : DiffUtil.ItemCallback<Patient>() {
    override fun areItemsTheSame(oldItem: Patient, newItem: Patient): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Patient, newItem: Patient): Boolean {
        return oldItem == newItem
    }
}