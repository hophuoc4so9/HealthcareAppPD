package com.example.healthcareapppd

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial

class AddAlarmFragment : Fragment(R.layout.fragment_add_alarm) {

    private lateinit var timePicker: TimePicker
    private lateinit var tvRepeat: TextView
    private lateinit var tvLabel: TextView
    private lateinit var tvRingtone: TextView
    private lateinit var switchSnooze: SwitchMaterial
    private lateinit var topAppBar: MaterialToolbar

    private var repeatDays = BooleanArray(7)
    private var label: String = "Báo thức"
    private var ringtoneUri: Uri? = null
    private var snooze: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timePicker = view.findViewById(R.id.timePicker)
        tvRepeat = view.findViewById(R.id.tvRepeat)
        tvLabel = view.findViewById(R.id.tvLabel)
        tvRingtone = view.findViewById(R.id.tvRingtone)
        switchSnooze = view.findViewById(R.id.switchSnooze)
        topAppBar = view.findViewById(R.id.topAppBarAdd)

        timePicker.setIs24HourView(true)

        // AppBar
        topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_save -> {
                    saveAlarm()
                    true
                }
                else -> false
            }
        }

        // Sự kiện
        view.findViewById<View>(R.id.itemRepeat).setOnClickListener { showRepeatDialog() }
        view.findViewById<View>(R.id.itemLabel).setOnClickListener { showLabelDialog() }
        view.findViewById<View>(R.id.itemRingtone).setOnClickListener { pickRingtone() }
        switchSnooze.setOnCheckedChangeListener { _, isChecked -> snooze = isChecked }
    }

    private fun showRepeatDialog() {
        val days = arrayOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
        AlertDialog.Builder(requireContext())
            .setTitle("Chọn ngày lặp lại")
            .setMultiChoiceItems(days, repeatDays) { _, which, isChecked ->
                repeatDays[which] = isChecked
            }
            .setPositiveButton("OK") { _, _ ->
                val selected = days.filterIndexed { i, _ -> repeatDays[i] }
                tvRepeat.text = if (selected.isEmpty()) "Không" else selected.joinToString(", ")
            }
            .show()
    }

    private fun showLabelDialog() {
        val editText = EditText(requireContext()).apply {
            setText(label)
            setSelection(text.length)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Đặt nhãn")
            .setView(editText)
            .setPositiveButton("OK") { _, _ ->
                label = editText.text.toString()
                tvLabel.text = label
            }
            .show()
    }

    private fun pickRingtone() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Chọn âm báo")
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ringtoneUri)
        }
        startActivityForResult(intent, 1001)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == AppCompatActivity.RESULT_OK) {
            ringtoneUri = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            tvRingtone.text = ringtoneUri?.let { RingtoneManager.getRingtone(requireContext(), it).getTitle(requireContext()) } ?: "Mặc định"
        }
    }

    private fun saveAlarm() {
        val alarm = Alarm(
            hour = timePicker.hour,
            minute = timePicker.minute,
            repeatDays = repeatDays,
            label = label,
            ringtoneUri = ringtoneUri?.toString(),
            snooze = snooze
        )

        // Gửi dữ liệu ngược về Fragment trước
        parentFragmentManager.setFragmentResult("addAlarmResult", bundleOf("alarm" to alarm))
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}
