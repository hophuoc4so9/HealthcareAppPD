package com.example.healthcareapppd.presentation.ui.user.reminder

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.healthcareapppd.R
import com.example.healthcareapppd.data.local.ReminderEntity
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText

class AddReminderFragment : Fragment() {

    private val viewModel: ReminderViewModel by lazy {
        ViewModelProvider(this)[ReminderViewModel::class.java]
    }

    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var editLabel: TextInputEditText
    private lateinit var switchActive: SwitchMaterial
    private lateinit var timePicker: TimePicker
    private lateinit var tvRepeat: TextView
    private lateinit var layoutDays: ViewGroup
    private lateinit var tvSound: TextView

    private lateinit var dayCheckboxes: List<CheckBox>
    private var selectedSoundUri: Uri? = null
    private var selectedSoundName: String = "Mặc định"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.reminder_fragment_add_item, container, false)

        // Ánh xạ view
        btnSave = view.findViewById(R.id.btn_save)
        btnCancel = view.findViewById(R.id.btn_cancel)
        editLabel = view.findViewById(R.id.edt_label)
        switchActive = view.findViewById(R.id.switch_snooze)
        timePicker = view.findViewById(R.id.timePicker)
        tvRepeat = view.findViewById(R.id.tv_repeat)
        layoutDays = view.findViewById(R.id.layout_days)
        tvSound = view.findViewById(R.id.tv_sound)

        // Danh sách checkbox ngày trong tuần
        dayCheckboxes = listOf(
            view.findViewById(R.id.cb_mon),
            view.findViewById(R.id.cb_tue),
            view.findViewById(R.id.cb_wed),
            view.findViewById(R.id.cb_thu),
            view.findViewById(R.id.cb_fri),
            view.findViewById(R.id.cb_sat),
            view.findViewById(R.id.cb_sun)
        )

        timePicker.setIs24HourView(true)

        // Nhấn “Lặp lại” -> Ẩn/hiện danh sách ngày
        tvRepeat.setOnClickListener {
            layoutDays.visibility =
                if (layoutDays.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        // Nhấn chọn âm thanh -> mở Ringtone Picker
        tvSound.setOnClickListener {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Chọn âm thanh nhắc hẹn")
                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedSoundUri)
            }
            startActivityForResult(intent, REQUEST_CODE_SOUND)
        }

        observeSaveResult()

        btnSave.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            val time = String.format("%02d:%02d", hour, minute)
            val label = editLabel.text?.toString()?.ifEmpty { "Không có nhãn" } ?: "Không có nhãn"
            val isActive = switchActive.isChecked

            // Lấy các ngày được chọn (đồng bộ format)
            val repeatDays = dayCheckboxes.filter { it.isChecked }
                .joinToString(", ") { it.text.toString() }

            // Lưu ReminderEntity vào DB
            val reminder = ReminderEntity(
                time = time,
                label = label,
                isActive = isActive,
                repeat = if (repeatDays.isEmpty()) null else repeatDays,
                title = selectedSoundUri?.toString() ?: "default"
            )

            viewModel.insert(reminder)
        }


        btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }

    // Nhận kết quả chọn âm thanh
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SOUND && resultCode == Activity.RESULT_OK) {
            selectedSoundUri = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            val ringtone = RingtoneManager.getRingtone(requireContext(), selectedSoundUri)
            selectedSoundName = ringtone?.getTitle(requireContext()) ?: "Mặc định"
            tvSound.text = "Âm thanh: $selectedSoundName"
        }
    }

    private fun observeSaveResult() {
        viewModel.saveResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess == true) {
                Toast.makeText(requireContext(), "Đã lưu nhắc hẹn thành công!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else if (isSuccess == false) {
                Toast.makeText(requireContext(), "Lỗi khi lưu nhắc hẹn!", Toast.LENGTH_SHORT).show()
            }
            viewModel.saveResult.removeObservers(viewLifecycleOwner)
        }
    }

    companion object {
        private const val REQUEST_CODE_SOUND = 1001
    }
}
