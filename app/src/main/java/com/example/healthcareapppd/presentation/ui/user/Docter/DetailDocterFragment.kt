package com.example.healthcareapppd.presentation.ui.user.Docter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.databinding.FragmentDetailDocterBinding
import com.example.healthcareapppd.domain.usecase.DateSlot
import com.example.healthcareapppd.domain.usecase.DoctorUsecase
import com.example.healthcareapppd.presentation.ui.user.Docter.book_apointment.DateSlotAdapter
import java.text.SimpleDateFormat
import java.util.*

class DetailDocterFragment : Fragment() {

    // Khai báo View Binding
    private var _binding: FragmentDetailDocterBinding? = null
    private val binding get() = _binding!!

    // Khai báo Adapter và Format ngày
    private lateinit var dateAdapter: DateSlotAdapter
    private val dateFormatBackend = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Sử dụng View Binding để inflate layout
        _binding = FragmentDetailDocterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Tải và hiển thị thông tin bác sĩ
        loadDoctorDetails()

        // 2. Thiết lập chức năng chọn ngày ngang (RecyclerView)
        setupDateRecyclerView()
        loadInitialDates()

        // 3. Xử lý nút quay lại
        binding.ivBackArrow.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 4. Xử lý nút Đặt lịch hẹn
        binding.btnBook.setOnClickListener {
            Toast.makeText(context, "Đã nhấn nút Đặt lịch hẹn! (Logic đặt lịch sẽ được triển khai sau)", Toast.LENGTH_SHORT).show()
            // Logic lấy ngày đã chọn và giờ đã chọn để tạo lịch hẹn
        }
    }

    private fun loadDoctorDetails() {
        // Lấy dữ liệu từ arguments
        val doctor = arguments?.getSerializable("KEY_DOCTOR") as? DoctorUsecase

        if (doctor != null) {
            // TRƯỜNG HỢP 1: CÓ DỮ LIỆU THẬT
            binding.doctorSumary.ivDoctorPhoto.setImageResource(doctor.photo)
            binding.doctorSumary.tvDoctorName.text = doctor.name
            binding.doctorSumary.tvDoctorSpeciality.text = doctor.speciality
            binding.doctorSumary.tvDoctorRating.text = doctor.rating.toString()
            binding.doctorSumary.tvDoctorDistance.text = doctor.distance
            binding.tvDoctorInfoDesc.text = "Bác sĩ ${doctor.name} là một chuyên gia hàng đầu trong lĩnh vực ${doctor.speciality}. Với hơn 10 năm kinh nghiệm, bác sĩ đã điều trị thành công cho hàng ngàn bệnh nhân."

        } else {
            // TRƯỜNG HỢP 2: KHÔNG CÓ DỮ LIỆU (doctor bị null) - Dữ liệu tạm thời
            val tmpDoctor = DoctorUsecase(
                R.drawable.ic_doctor, // Dùng một icon mặc định
                "Bác sĩ (Mẫu)",
                "Chuyên Khoa Tổng Quát",
                4.5f,
                "2.5 km"
            )

            // Gán dữ liệu tạm thời lên giao diện
            binding.doctorSumary.ivDoctorPhoto.setImageResource(tmpDoctor.photo)
            binding.doctorSumary.tvDoctorName.text = tmpDoctor.name
            binding.doctorSumary.tvDoctorSpeciality.text = tmpDoctor.speciality
            binding.doctorSumary.tvDoctorRating.text = tmpDoctor.rating.toString()
            binding.doctorSumary.tvDoctorDistance.text = tmpDoctor.distance
            binding.tvDoctorInfoDesc.text = "Không tìm thấy dữ liệu bác sĩ. Đây là thông tin mẫu để kiểm thử giao diện."
        }
    }

    private fun setupDateRecyclerView() {
        // Định nghĩa Callback: Fragment xử lý logic khi một ngày được chọn
        val onDateSelected: (DateSlot) -> Unit = { dateSlot ->
            // Logic nghiệp vụ: Cập nhật UI khác (ví dụ: tải các Time Slot cho ngày này)
            Toast.makeText(context, "Đã chọn ngày: ${dateSlot.fullDate}", Toast.LENGTH_SHORT).show()
            // Tại đây, bạn sẽ gọi ViewModel để tải các khung giờ rảnh cho ngày đã chọn
            // viewModel.loadTimeSlots(dateSlot.fullDate)
        }

        dateAdapter = DateSlotAdapter(onDateSelected)

        // Đảm bảo RecyclerView sử dụng LinearLayoutManager với orientation HORIZONTAL
        binding.rvDatePicker.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = dateAdapter
        }
    }

    // Tạo dữ liệu giả lập cho 7 ngày tiếp theo
    private fun loadInitialDates() {
        val calendar = Calendar.getInstance()
        val dates = mutableListOf<DateSlot>()
        val dayFormat = SimpleDateFormat("EE", Locale.ENGLISH) // Để lấy T2, T3...
        val dayFormatDisplay = SimpleDateFormat("dd", Locale.getDefault())

        for (i in 0 until 7) {
            val fullDate = dateFormatBackend.format(calendar.time)
            var dayOfWeek = dayFormat.format(calendar.time)
            val date = dayFormatDisplay.format(calendar.time)

            // Chuyển đổi tên ngày tiếng Anh sang tiếng Việt
            dayOfWeek = when (dayOfWeek) {
                "Mon" -> "T2"
                "Tue" -> "T3"
                "Wed" -> "T4"
                "Thu" -> "T5"
                "Fri" -> "T6"
                "Sat" -> "T7"
                "Sun" -> "CN"
                else -> dayOfWeek
            }

            // Mặc định chọn ngày đầu tiên (hôm nay)
            val isSelected = i == 0

            dates.add(DateSlot(
                fullDate = fullDate,
                dayOfWeek = dayOfWeek,
                date = date,
                isSelected = isSelected
            ))

            calendar.add(Calendar.DAY_OF_YEAR, 1) // Chuyển sang ngày tiếp theo
        }

        dateAdapter.submitList(dates)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
