package com.example.healthcareapppd.presentation.ui.user.reminder

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.healthcareapppd.R
import com.example.healthcareapppd.data.local.ReminderEntity
import com.example.healthcareapppd.databinding.ReminderFragmentEditItemBinding

class EditReminderFragment : Fragment() {

    private lateinit var binding: ReminderFragmentEditItemBinding
    private lateinit var viewModel: ReminderViewModel
    private var reminder: ReminderEntity? = null
    private var selectedSoundUri: Uri? = null

    private val soundPicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedSoundUri = uri
                    val ringtone =
                        RingtoneManager.getRingtone(requireContext(), uri)
                    val title = ringtone?.getTitle(requireContext()) ?: "Tùy chỉnh"
                    binding.tvSound.text = "Âm thanh: $title"
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ReminderFragmentEditItemBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ReminderViewModel::class.java]

        reminder = arguments?.getParcelable("reminder")

        binding.timePicker.setIs24HourView(true)
        reminder?.let { bindReminderData(it) }

        binding.tvRepeat.setOnClickListener {
            binding.layoutDays.visibility =
                if (binding.layoutDays.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        binding.tvSound.setOnClickListener {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            }
            soundPicker.launch(intent)
        }

        binding.btnUpdate.setOnClickListener { updateReminder() }
        binding.btnCancel.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.btnDelete.setOnClickListener { deleteReminder() }

        observeUpdateResult()

        return binding.root
    }

    private fun bindReminderData(reminder: ReminderEntity) {
        binding.edtLabel.setText(reminder.label)
        binding.switchSnooze.isChecked = reminder.isActive

        // Hiển thị thời gian
        val (hour, minute) = reminder.time.split(":").map { it.toInt() }
        binding.timePicker.hour = hour
        binding.timePicker.minute = minute

        // ✅ Hiển thị ngày lặp lại (so khớp đúng với format "Thứ hai")
        reminder.repeat?.let { repeatString ->
            val repeatDays = repeatString.split(",").map { it.trim() }
            val mapping = mapOf(
                R.id.cb_mon to "Thứ hai",
                R.id.cb_tue to "Thứ ba",
                R.id.cb_wed to "Thứ tư",
                R.id.cb_thu to "Thứ năm",
                R.id.cb_fri to "Thứ sáu",
                R.id.cb_sat to "Thứ bảy",
                R.id.cb_sun to "Chủ nhật"
            )
            mapping.forEach { (id, dayName) ->
                view?.findViewById<CheckBox>(id)?.isChecked = dayName in repeatDays
            }
        }

        // ✅ Hiển thị lại âm thanh
        if (!reminder.title.isNullOrEmpty() && reminder.title != "default") {
            try {
                selectedSoundUri = Uri.parse(reminder.title)
                val ringtone = RingtoneManager.getRingtone(requireContext(), selectedSoundUri)
                val soundTitle = ringtone?.getTitle(requireContext()) ?: "Tùy chỉnh"
                binding.tvSound.text = "Âm thanh: $soundTitle"
            } catch (e: Exception) {
                binding.tvSound.text = "Âm thanh: Mặc định"
            }
        } else {
            binding.tvSound.text = "Âm thanh: Mặc định"
        }
    }

    private fun updateReminder() {
        reminder?.let {
            val hour = binding.timePicker.hour
            val minute = binding.timePicker.minute
            val time = String.format("%02d:%02d", hour, minute)
            val label = binding.edtLabel.text.toString()
            val isActive = binding.switchSnooze.isChecked
            val selectedDays = getSelectedDays()
            val repeatDays = if (selectedDays.isNotEmpty()) selectedDays.joinToString(", ") else null
            val sound = selectedSoundUri?.toString() ?: "default"

            val updated = it.copy(
                time = time,
                label = label,
                isActive = isActive,
                repeat = repeatDays,
                title = sound
            )

            viewModel.update(updated)
        }
    }

    private fun deleteReminder() {
        reminder?.let {
            viewModel.delete(it)
            Toast.makeText(requireContext(), "Đã xóa nhắc hẹn", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    private fun getSelectedDays(): List<String> {
        val days = mutableListOf<String>()
        fun addIfChecked(id: Int, text: String) {
            view?.findViewById<CheckBox>(id)?.let { if (it.isChecked) days.add(text) }
        }
        addIfChecked(R.id.cb_mon, "Thứ 2")
        addIfChecked(R.id.cb_tue, "Thứ 3")
        addIfChecked(R.id.cb_wed, "Thứ 4")
        addIfChecked(R.id.cb_thu, "Thứ 5")
        addIfChecked(R.id.cb_fri, "Thứ 6")
        addIfChecked(R.id.cb_sat, "Thứ 7")
        addIfChecked(R.id.cb_sun, "Chủ nhật")
        return days
    }

    private fun observeUpdateResult() {
        viewModel.updateResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess == true) {
                Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else if (isSuccess == false) {
                Toast.makeText(requireContext(), "Lỗi khi cập nhật!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
