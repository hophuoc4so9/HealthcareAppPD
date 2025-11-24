// File: com/example/healthcareapppd/presentation/ui/user/Docter/DetailDocterFragment.kt
package com.example.healthcareapppd.presentation.ui.user.Docter

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.data.api.model.AvailabilitySlot
import com.example.healthcareapppd.data.api.model.DoctorProfile
import com.example.healthcareapppd.domain.usecase.appointment.BookAppointmentUseCase
import com.example.healthcareapppd.domain.usecase.appointment.GetDoctorAvailabilitySlotsUseCase
import com.example.healthcareapppd.domain.usecase.chat.CreateConversationUseCase
import com.example.healthcareapppd.domain.usecase.reminder.CreateReminderUseCase
import com.example.healthcareapppd.presentation.adapter.TimeSlotAdapter
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class DetailDocterFragment : Fragment() {

    private lateinit var timeSlotAdapter: TimeSlotAdapter
    private val getDoctorAvailabilitySlotsUseCase = GetDoctorAvailabilitySlotsUseCase()
    private val bookAppointmentUseCase = BookAppointmentUseCase()
    private val createReminderUseCase = CreateReminderUseCase()
    private val createConversationUseCase = CreateConversationUseCase()
    
    private var selectedDate: String = ""
    private var currentDoctor: DoctorProfile? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail_docter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Lấy dữ liệu từ arguments
        val doctor = arguments?.getSerializable("KEY_DOCTOR") as? DoctorProfile
        currentDoctor = doctor

        // Tìm tất cả các view cần thiết
        val photo: ImageView = view.findViewById(R.id.iv_doctor_photo)
        val name: TextView = view.findViewById(R.id.tv_doctor_name)
        val speciality: TextView = view.findViewById(R.id.tv_doctor_speciality)
        val distance: TextView = view.findViewById(R.id.tv_doctor_distance)
        val doctorInfoDesc: TextView = view.findViewById(R.id.tv_doctor_info_desc)
        val backButton: ImageView = view.findViewById(R.id.iv_back_arrow)
        
        // Appointment booking views
        val tvSelectedDate: TextView = view.findViewById(R.id.tv_selected_date)
        val btnSelectDate: Button = view.findViewById(R.id.btn_select_date)
        val rvTimeSlots: RecyclerView = view.findViewById(R.id.rv_time_slots)
        val etPatientNotes: TextInputEditText = view.findViewById(R.id.et_patient_notes)
        val btnBookAppointment: Button = view.findViewById(R.id.btn_book_appointment)
        val btnChatWithDoctor: Button = view.findViewById(R.id.btn_chat_with_doctor)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)

        if (doctor != null) {
            Log.d("DetailFragment", "Doctor data found: ${doctor.fullName}")

            photo.setImageResource(R.drawable.ic_doctor)
            name.text = doctor.fullName
            speciality.text = doctor.specialization
            

            // Hiển thị bệnh viện
            distance.text = doctor.clinicAddress ?: "Chưa cập nhật"
            
            // Mô tả chi tiết
            val description = buildString {
                append("${doctor.bio}")
                

                
                append("\n\n📋 Thông tin liên hệ:")
                
                if (!doctor.email.isNullOrEmpty()) {
                    append("\nEmail: ${doctor.email}")
                }
                

                
                if (!doctor.clinicAddress.isNullOrEmpty()) {
                    append("\nBệnh viện: ${doctor.clinicAddress}")
                }
                

            }
            
            doctorInfoDesc.text = description

        } else {
            Log.e("DetailFragment", "Doctor data is NULL")

            photo.setImageResource(R.drawable.ic_doctor)
            name.text = "Không có thông tin"
            speciality.text = "N/A"
            distance.text = "N/A"
            doctorInfoDesc.text = "Thông tin bác sĩ không khả dụng. Vui lòng thử lại sau."
        }

        // Setup time slots RecyclerView
        timeSlotAdapter = TimeSlotAdapter(emptyList<AvailabilitySlot>()) { slot ->
            btnBookAppointment.isEnabled = true
        }
        
        rvTimeSlots.apply {
            adapter = timeSlotAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        // Date picker
        btnSelectDate.setOnClickListener {
            showDatePicker(tvSelectedDate) { date ->
                selectedDate = date
                loadTimeSlots(progressBar)
            }
        }

        // Book appointment button
        btnBookAppointment.setOnClickListener {
            val selectedSlot = timeSlotAdapter.getSelectedSlot()
            if (selectedSlot != null && doctor != null) {
                bookAppointment(
                    doctorUserId = doctor.userId,
                    slotId = selectedSlot.id,
                    notes = etPatientNotes.text?.toString(),
                    progressBar = progressBar
                )
            } else {
                Toast.makeText(context, "Vui lòng chọn khung giờ", Toast.LENGTH_SHORT).show()
            }
        }

        // Chat with doctor button
        btnChatWithDoctor.setOnClickListener {
            if (doctor != null) {
                startChatWithDoctor(doctor.userId, doctor.fullName)
            } else {
                Toast.makeText(context, "Không tìm thấy thông tin bác sĩ", Toast.LENGTH_SHORT).show()
            }
        }

        // Xử lý nút quay lại
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun showDatePicker(tvSelectedDate: TextView, onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                
                // Format cho UI
                val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale("vi"))
                tvSelectedDate.text = displayFormat.format(calendar.time)
                
                // Format cho API (YYYY-MM-DD)
                val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale("vi"))
                val dateString = apiFormat.format(calendar.time)
                
                onDateSelected(dateString)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // Chỉ cho phép chọn ngày từ hôm nay trở đi
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun loadTimeSlots(progressBar: ProgressBar) {
        val doctor = currentDoctor ?: return
        
        if (selectedDate.isEmpty()) {
            Toast.makeText(context, "Vui lòng chọn ngày", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            
            val result = getDoctorAvailabilitySlotsUseCase(requireContext(), doctor.userId, selectedDate)
            
            progressBar.visibility = View.GONE
            
            result.onSuccess { slots ->
                if (slots.isEmpty()) {
                    Toast.makeText(context, "Không có khung giờ nào khả dụng trong ngày này", Toast.LENGTH_SHORT).show()
                }
                timeSlotAdapter.updateTimeSlots(slots)
            }.onFailure { error ->
                Toast.makeText(context, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("DetailDocterFragment", "Error loading time slots", error)
            }
        }
    }

    private fun bookAppointment(
        doctorUserId: String,
        slotId: String,
        notes: String?,
        progressBar: ProgressBar
    ) {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            view?.findViewById<Button>(R.id.btn_book_appointment)?.isEnabled = false
            
            val result = bookAppointmentUseCase(
                context = requireContext(),
                doctorUserId = doctorUserId,
                availabilitySlotId = slotId,
                patientNotes = notes
            )
            
            progressBar.visibility = View.GONE
            view?.findViewById<Button>(R.id.btn_book_appointment)?.isEnabled = true
            
            result.onSuccess { appointment ->
                Toast.makeText(context, "Đặt lịch hẹn thành công!", Toast.LENGTH_LONG).show()
                
                // Tạo reminder nhắc trước 30 phút
                createAppointmentReminder(appointment)
                
                // Navigate back or to appointments list
                parentFragmentManager.popBackStack()
            }.onFailure { error ->
                Toast.makeText(context, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("DetailDocterFragment", "Error booking appointment", error)
            }
        }
    }
    
    private fun createAppointmentReminder(appointment: com.example.healthcareapppd.data.api.model.Appointment) {
        lifecycleScope.launch {
            try {
                // Parse startTime and subtract 30 minutes
                val startTime = appointment.startTime ?: return@launch
                val instant = Instant.parse(startTime)
                val reminderTime = instant.minusSeconds(30 * 60) // 30 minutes before
                
                // Format reminder time to ISO 8601
                val reminderTimeString = reminderTime.toString()
                
                // Format display time
                val formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
                    .withZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                val displayTime = formatter.format(instant)
                
                val doctorName = appointment.doctorName ?: "bác sĩ"
                
                createReminderUseCase(
                    context = requireContext(),
                    title = "Nhắc nhở lịch hẹn khám bệnh",
                    description = "Bạn có lịch hẹn với $doctorName lúc $displayTime. Vui lòng chuẩn bị trước 30 phút.",
                    reminderType = "appointment",
                    oneTimeAt = reminderTimeString
                ).onSuccess {
                    Log.d("DetailDocterFragment", "Reminder created successfully")
                }.onFailure { error ->
                    Log.e("DetailDocterFragment", "Failed to create reminder: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e("DetailDocterFragment", "Error creating reminder", e)
            }
        }
    }
    
    private fun startChatWithDoctor(doctorUserId: String, doctorName: String) {
        lifecycleScope.launch {
            try {
                // Create or get existing conversation
                createConversationUseCase(requireContext(), doctorUserId).onSuccess { conversation ->
                    // Navigate to chat fragment
                    val bundle = Bundle().apply {
                        putString("conversationId", conversation.id)
                        putString("conversationName", doctorName)
                    }
                    findNavController().navigate(R.id.action_detailDocter_to_chat, bundle)
                }.onFailure { error ->
                    Toast.makeText(
                        requireContext(),
                        "Lỗi: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("DetailDocterFragment", "Error creating conversation", error)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Không thể mở chat: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("DetailDocterFragment", "Error starting chat", e)
            }
        }
    }
}