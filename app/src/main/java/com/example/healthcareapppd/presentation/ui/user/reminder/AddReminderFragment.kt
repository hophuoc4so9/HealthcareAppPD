package com.example.healthcareapppd.presentation.ui.user.reminder

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.healthcareapppd.R
import com.example.healthcareapppd.domain.usecase.reminder.CreateReminderUseCase
import com.example.healthcareapppd.utils.TokenManager
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch


class AddReminderFragment : Fragment() {

    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var editLabel: TextInputEditText
    private lateinit var editDescription: TextInputEditText
    private lateinit var switchActive: SwitchMaterial
    private lateinit var timePicker: TimePicker
    private lateinit var spinnerType: Spinner
    private lateinit var progressBar: ProgressBar
    private val createReminderUseCase = CreateReminderUseCase()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.reminder_fragment_add_item, container, false)
        
        btnSave = view.findViewById(R.id.btn_save)
        btnCancel = view.findViewById(R.id.btn_cancel)
        editLabel = view.findViewById(R.id.edt_label)
        editDescription = view.findViewById(R.id.edt_description)
        switchActive = view.findViewById(R.id.switchActive)
        timePicker = view.findViewById(R.id.timePicker)
        spinnerType = view.findViewById(R.id.spinnerType)
        progressBar = view.findViewById(R.id.progressBar)

        timePicker.setIs24HourView(true)

        // Setup reminder type spinner
        val reminderTypes = arrayOf(
            "medication" to "Uống thuốc",
            "sleep" to "Giấc ngủ",
            "appointment" to "Lịch hẹn",
            "general" to "Chung"
        )
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            reminderTypes.map { it.second }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter

        btnSave.setOnClickListener {
            createReminder(reminderTypes)
        }

        btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }

    private fun createReminder(reminderTypes: Array<Pair<String, String>>) {
        val title = editLabel.text?.toString()?.trim()
        if (title.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show()
            return
        }

        val hour = timePicker.hour
        val minute = timePicker.minute
        val description = editDescription.text?.toString()?.trim()
        val reminderTypeValue = reminderTypes[spinnerType.selectedItemPosition].first
        
        // Create cron expression for daily reminder at selected time
        // Format: "minute hour * * *" (daily)
        val cronExpression = "$minute $hour * * *"

        progressBar.visibility = View.VISIBLE
        btnSave.isEnabled = false

        lifecycleScope.launch {
            createReminderUseCase(
                context = requireContext(),
                title = title,
                description = description,
                reminderType = reminderTypeValue,
                cronExpression = cronExpression,
                oneTimeAt = null,
                timezoneName = "Asia/Ho_Chi_Minh"
            ).fold(
                onSuccess = {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Đã tạo nhắc nhở", Toast.LENGTH_SHORT).show()
                    
                    // Notify ReminderListFragment to reload
                    parentFragmentManager.setFragmentResult("addReminderKey", Bundle())
                    parentFragmentManager.popBackStack()
                },
                onFailure = { error ->
                    progressBar.visibility = View.GONE
                    btnSave.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        "Lỗi: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }
}
