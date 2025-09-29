package com.example.healthcareapppd.presentation.ui.user.reminder

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import com.example.healthcareapppd.R
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText


class AddReminderFragment : Fragment() {

    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var editLabel: TextInputEditText
    private lateinit var switchActive: SwitchMaterial
    private lateinit var timePicker: TimePicker

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.reminder_fragment_add_item, container, false)

        btnSave = view.findViewById(R.id.btn_save)
        btnCancel = view.findViewById(R.id.btn_cancel)
        editLabel = view.findViewById(R.id.edt_label)
        switchActive = view.findViewById(R.id.switchActive)
        timePicker = view.findViewById(R.id.timePicker)

        val timePicker = view.findViewById<TimePicker>(R.id.timePicker)
        timePicker.setIs24HourView(true)

        btnSave.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            val time = String.format("%02d:%02d", hour, minute)
            val label = editLabel.text?.toString()?.ifEmpty { "Không có nhãn" } ?: "Không có nhãn"
            val isActive = switchActive.isChecked

            // Gửi dữ liệu về ReminderListFragment
            val result = Bundle().apply {
                putString("time", time)
                putString("label", label)
                putBoolean("isActive", isActive)
            }
            parentFragmentManager.setFragmentResult("addReminderKey", result)

            // Quay về danh sách
            parentFragmentManager.popBackStack()
        }

        btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }
}
