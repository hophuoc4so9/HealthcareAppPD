package com.example.healthcareapppd.presentation.ui.user.reminder

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.data.api.model.Appointment
import com.example.healthcareapppd.data.api.model.Reminder
import com.example.healthcareapppd.domain.usecase.appointment.GetMyAppointmentsUseCase
import com.example.healthcareapppd.domain.usecase.reminder.DeleteReminderUseCase
import com.example.healthcareapppd.domain.usecase.reminder.GetRemindersUseCase
import com.example.healthcareapppd.domain.usecase.reminder.ToggleReminderUseCase
import com.example.healthcareapppd.presentation.adapter.ReminderAppointmentAdapter
import com.example.healthcareapppd.utils.TokenManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class ReminderListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReminderAppointmentAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var fabEdit: FloatingActionButton
    private val reminders = mutableListOf<Reminder>()
    private val appointments = mutableListOf<Appointment>()
    private val getRemindersUseCase = GetRemindersUseCase()
    private val getMyAppointmentsUseCase = GetMyAppointmentsUseCase()
    private val toggleReminderUseCase = ToggleReminderUseCase()
    private val deleteReminderUseCase = DeleteReminderUseCase()
    private var selectedReminder: Reminder? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.reminder_fragment_item_list, container, false)

        recyclerView = view.findViewById(R.id.recycler_view_reminders)
        progressBar = view.findViewById(R.id.progressBar)
        fabAdd = view.findViewById(R.id.fab_add)
        fabEdit = view.findViewById(R.id.fab_edit)
        
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        adapter = ReminderAppointmentAdapter(
            onReminderToggle = { reminder, isActive ->
                toggleReminder(reminder, isActive)
            },
            onReminderDelete = { reminder ->
                showDeleteConfirmation(reminder)
            },
            onAppointmentClick = { appointment ->
                // TODO: Navigate to appointment details
                Toast.makeText(requireContext(), "Lịch hẹn: ${appointment.doctorName}", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = adapter
        
        // Nút thêm reminder mới
        fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_reminderList_to_addReminder)
        }
        
        // Nút sửa reminder đã chọn
        fabEdit.setOnClickListener {
            selectedReminder?.let { reminder ->
                // Navigate to edit screen with reminder data
                val bundle = Bundle().apply {
                    putString("reminder_id", reminder.id)
                    putString("title", reminder.title)
                    putString("description", reminder.description)
                    putString("reminder_type", reminder.reminderType)
                    putString("cron_expression", reminder.cronExpression)
                }
                findNavController().navigate(R.id.action_reminderList_to_editReminder, bundle)
            }
        }
        
        // Ẩn nút edit ban đầu
        fabEdit.visibility = View.GONE

        // Nhận dữ liệu từ AddReminderFragment nếu có (sau khi tạo mới)
        parentFragmentManager.setFragmentResultListener("addReminderKey", viewLifecycleOwner) { _, _ ->
            // Reload reminders after adding new one
            loadReminders()
        }

        loadReminders()

        return view
    }

    private fun loadReminders() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            // Load reminders
            val remindersResult = getRemindersUseCase(requireContext())
            
            // Load appointments
            val appointmentsResult = getMyAppointmentsUseCase(
                context = requireContext(),
                status = "scheduled" // Only show scheduled appointments
            )
            
            progressBar.visibility = View.GONE
            
            val reminderList = remindersResult.getOrNull() ?: emptyList()
            val appointmentList = appointmentsResult.getOrNull() ?: emptyList()
            
            reminders.clear()
            reminders.addAll(reminderList)
            appointments.clear()
            appointments.addAll(appointmentList)
            
            adapter.updateItems(appointmentList, reminderList)
            
            if (reminderList.isEmpty() && appointmentList.isEmpty()) {
                Toast.makeText(requireContext(), "Chưa có lịch hẹn hoặc nhắc nhở nào", Toast.LENGTH_SHORT).show()
            }
            
            // Show errors if any
            remindersResult.onFailure { error ->
                Toast.makeText(
                    requireContext(),
                    "Lỗi tải nhắc nhở: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            
            appointmentsResult.onFailure { error ->
                Toast.makeText(
                    requireContext(),
                    "Lỗi tải lịch hẹn: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun toggleReminder(reminder: Reminder, isActive: Boolean) {
        lifecycleScope.launch {
            toggleReminderUseCase(requireContext(), reminder.id, isActive).fold(
                onSuccess = { updatedReminder ->
                    adapter.updateReminder(updatedReminder)
                    Toast.makeText(
                        requireContext(),
                        if (isActive) "Đã bật nhắc nhở" else "Đã tắt nhắc nhở",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onFailure = { error ->
                    // Revert switch state
                    adapter.updateReminder(reminder)
                    Toast.makeText(
                        requireContext(),
                        "Lỗi: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    private fun showDeleteConfirmation(reminder: Reminder) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Xóa nhắc nhở")
            .setMessage("Bạn có chắc chắn muốn xóa nhắc nhở \"${reminder.title}\"?")
            .setPositiveButton("Xóa") { _, _ ->
                deleteReminder(reminder)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteReminder(reminder: Reminder) {
        lifecycleScope.launch {
            deleteReminderUseCase(requireContext(), reminder.id).fold(
                onSuccess = {
                    adapter.removeReminder(reminder)
                    Toast.makeText(requireContext(), "Đã xóa nhắc nhở", Toast.LENGTH_SHORT).show()
                },
                onFailure = { error ->
                    Toast.makeText(
                        requireContext(),
                        "Lỗi xóa: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload reminders when fragment resumes
        loadReminders()
    }
}
