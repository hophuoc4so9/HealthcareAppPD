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
import androidx.navigation.fragment.findNavController
import com.example.healthcareapppd.R
import com.example.healthcareapppd.domain.usecase.reminder.UpdateReminderUseCase
import com.example.healthcareapppd.utils.TokenManager
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch


class EditReminderFragment : Fragment() {

    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var editLabel: TextInputEditText
    private lateinit var editDescription: TextInputEditText
    private lateinit var switchActive: SwitchMaterial
    private lateinit var timePicker: TimePicker
    private lateinit var spinnerType: Spinner
    private lateinit var progressBar: ProgressBar
    private val updateReminderUseCase = UpdateReminderUseCase()
    private lateinit var tokenManager: TokenManager
    
    private var reminderId: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.reminder_fragment_edit_item, container, false)

        tokenManager = TokenManager.init(requireContext())
        
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

        // Load data from arguments
        loadReminderData(reminderTypes)

        btnSave.setOnClickListener {
            updateReminder(reminderTypes)
        }

        btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }

    private fun loadReminderData(reminderTypes: Array<Pair<String, String>>) {
        arguments?.let { args ->
            reminderId = args.getString("reminder_id")
            val title = args.getString("title")
            val description = args.getString("description")
            val reminderType = args.getString("reminder_type")
            val cronExpression = args.getString("cron_expression")

            editLabel.setText(title)
            editDescription.setText(description)

            // Set spinner selection
            reminderType?.let { type ->
                val index = reminderTypes.indexOfFirst { it.first == type }
                if (index >= 0) {
                    spinnerType.setSelection(index)
                }
            }

            // Parse cron expression to set time
            cronExpression?.let { cron ->
                val parts = cron.split(" ")
                if (parts.size >= 2) {
                    val minute = parts[0].toIntOrNull() ?: 0
                    val hour = parts[1].toIntOrNull() ?: 0
                    timePicker.hour = hour
                    timePicker.minute = minute
                }
            }
        }
    }

    private fun updateReminder(reminderTypes: Array<Pair<String, String>>) {
        val title = editLabel.text?.toString()?.trim()
        if (title.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show()
            return
        }

        val id = reminderId
        if (id == null) {
            Toast.makeText(requireContext(), "Lỗi: Không tìm thấy ID reminder", Toast.LENGTH_SHORT).show()
            return
        }

        val token = tokenManager.getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
            return
        }

        val hour = timePicker.hour
        val minute = timePicker.minute
        val description = editDescription.text?.toString()?.trim()
        val reminderTypeValue = reminderTypes[spinnerType.selectedItemPosition].first
        
        // Create cron expression for daily reminder at selected time
        val cronExpression = "$minute $hour * * *"

        progressBar.visibility = View.VISIBLE
        btnSave.isEnabled = false

        lifecycleScope.launch {
            updateReminderUseCase(
                token = token,
                reminderId = id,
                title = title,
                description = description,
                reminderType = reminderTypeValue,
                cronExpression = cronExpression,
                oneTimeAt = null,
                timezoneName = "Asia/Ho_Chi_Minh"
            ).fold(
                onSuccess = {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Đã cập nhật nhắc nhở", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
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
