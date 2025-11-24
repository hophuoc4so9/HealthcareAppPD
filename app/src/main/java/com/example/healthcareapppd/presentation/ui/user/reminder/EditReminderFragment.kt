package com.example.healthcareapppd.presentation.ui.user.reminder

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
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
import android.util.Log


class EditReminderFragment : Fragment() {

    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var editLabel: TextInputEditText
    private lateinit var switchActive: SwitchMaterial
    private lateinit var timePicker: TimePicker
    private lateinit var spinnerType: android.widget.Spinner
    private lateinit var progressBar: ProgressBar
    private lateinit var checkboxMon: android.widget.CheckBox
    private lateinit var checkboxTue: android.widget.CheckBox
    private lateinit var checkboxWed: android.widget.CheckBox
    private lateinit var checkboxThu: android.widget.CheckBox
    private lateinit var checkboxFri: android.widget.CheckBox
    private lateinit var checkboxSat: android.widget.CheckBox
    private lateinit var checkboxSun: android.widget.CheckBox
    private lateinit var editDescription: TextInputEditText
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
            checkboxMon = view.findViewById(R.id.checkboxMon)
            checkboxTue = view.findViewById(R.id.checkboxTue)
            checkboxWed = view.findViewById(R.id.checkboxWed)
            checkboxThu = view.findViewById(R.id.checkboxThu)
            checkboxFri = view.findViewById(R.id.checkboxFri)
            checkboxSat = view.findViewById(R.id.checkboxSat)
            checkboxSun = view.findViewById(R.id.checkboxSun)

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
            val repeatDayIndex = args.getInt("repeat_day_index", 0)

            editLabel.setText(title)
            editDescription.setText(description)

            // Set spinner selection
            reminderType?.let { type ->
                val index = reminderTypes.indexOfFirst { it.first == type }
                if (index >= 0) {
                    spinnerType.setSelection(index)
                }
            }

            // Parse cron expression để set các checkbox ngày
            cronExpression?.let { cron ->
                val parts = cron.split(" ")
                if (parts.size >= 5) {
                    val days = parts[4].split(",")
                    checkboxMon.isChecked = days.contains("1")
                    checkboxTue.isChecked = days.contains("2")
                    checkboxWed.isChecked = days.contains("3")
                    checkboxThu.isChecked = days.contains("4")
                    checkboxFri.isChecked = days.contains("5")
                    checkboxSat.isChecked = days.contains("6")
                    checkboxSun.isChecked = days.contains("0")
                    val minute = parts[0].toIntOrNull() ?: 0
                    val hour = parts[1].toIntOrNull() ?: 0
                    timePicker.hour = hour
                    timePicker.minute = minute
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
        val daysChecked = mutableListOf<Int>()
        if (checkboxMon.isChecked) daysChecked.add(1)
        if (checkboxTue.isChecked) daysChecked.add(2)
        if (checkboxWed.isChecked) daysChecked.add(3)
        if (checkboxThu.isChecked) daysChecked.add(4)
        if (checkboxFri.isChecked) daysChecked.add(5)
        if (checkboxSat.isChecked) daysChecked.add(6)
        if (checkboxSun.isChecked) daysChecked.add(0)

        val cronExpression: String?
        var oneTimeAt: String? = null
        if (daysChecked.isEmpty()) {
            // Nếu không chọn ngày nào, nhắc 1 lần hôm nay hoặc ngày mai
            val now = java.util.Calendar.getInstance()
            val reminderTime = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, minute)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            if (reminderTime.timeInMillis > now.timeInMillis) {
                oneTimeAt = sdf.format(reminderTime.time)
            } else {
                reminderTime.add(java.util.Calendar.DAY_OF_MONTH, 1)
                oneTimeAt = sdf.format(reminderTime.time)
            }
            cronExpression = null
        } else {
            // Nếu chọn nhiều ngày, tạo cronExpression cho các ngày đó
            val daysString = daysChecked.joinToString(",")
            cronExpression = "$minute $hour * * $daysString"
        }

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
                oneTimeAt = oneTimeAt,
                timezoneName = "Asia/Ho_Chi_Minh"
            ).fold(
                onSuccess = {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Đã cập nhật nhắc nhở", Toast.LENGTH_SHORT).show()
                    // Đặt lại alarm cho reminder vừa sửa
                    if (oneTimeAt != null) {
                        scheduleLocalNotification(title ?: "", description, oneTimeAt)
                    } else if (cronExpression != null) {
                        scheduleRecurringNotification(title ?: "", description, cronExpression)
                    }
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

    private fun scheduleLocalNotification(title: String, description: String?, oneTimeAt: String) {
        try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val date = sdf.parse(oneTimeAt)
            date?.let {
                scheduleReminderAlarm(title, description ?: "", it.time)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scheduleRecurringNotification(title: String, description: String?, cronExpression: String) {
        try {
            val parts = cronExpression.split(" ")
            if (parts.size >= 5) {
                val minute = parts[0].toIntOrNull() ?: 0
                val hour = parts[1].toIntOrNull() ?: 0
                val days = parts[4].split(",").mapNotNull { it.toIntOrNull() }
                // Đặt alarm cho từng ngày trong tuần
                days.forEach { day ->
                    val nextTriggerTime = calculateNextDayTriggerTime(hour, minute, day)
                    Log.d("EditReminderFragment", "Đặt alarm lặp lại: $title, day=$day, time=$nextTriggerTime")
                    scheduleReminderAlarm(title, description ?: "", nextTriggerTime)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateNextDayTriggerTime(hour: Int, minute: Int, cronDay: Int): Long {
        val now = java.util.Calendar.getInstance()
        val trigger = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val calendarDay = if (cronDay == 0) java.util.Calendar.SUNDAY else cronDay + 1
        for (i in 0..7) {
            if (trigger.get(java.util.Calendar.DAY_OF_WEEK) == calendarDay && trigger.timeInMillis > now.timeInMillis) {
                return trigger.timeInMillis
            }
            trigger.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        return trigger.timeInMillis
    }

    private fun scheduleReminderAlarm(title: String, description: String, triggerTimeMillis: Long) {
        Log.d("EditReminderFragment", "scheduleReminderAlarm: $title, $description, triggerTimeMillis=$triggerTimeMillis, time=" +
            java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(triggerTimeMillis)))
        if (triggerTimeMillis < System.currentTimeMillis()) {
            Log.w("EditReminderFragment", "Không đặt alarm vì thời gian đã qua: $triggerTimeMillis")
            return
        }
        try {
            val intent = android.content.Intent(requireContext(),
                com.example.healthcareapppd.presentation.receiver.ReminderReceiver::class.java).apply {
                putExtra("title", title)
                putExtra("description", description)
            }
            val requestCode = (title + triggerTimeMillis).hashCode()
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                requireContext(),
                requestCode,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = requireContext().getSystemService(android.content.Context.ALARM_SERVICE)
                as android.app.AlarmManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        android.app.AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                } catch (e: SecurityException) {
                    Log.e("EditReminderFragment", "scheduleReminderAlarm error: ${e.message}", e)
                    Toast.makeText(requireContext(),
                        "Bạn cần cấp quyền Báo thức chính xác cho ứng dụng trong Cài đặt > Ứng dụng > HealthcareAppPD > Quyền đặc biệt > Báo thức chính xác.",
                        Toast.LENGTH_LONG).show()
                    val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    intent.data = android.net.Uri.parse("package:" + requireContext().packageName)
                    startActivity(intent)
                }
            } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    android.app.AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            Log.e("EditReminderFragment", "scheduleReminderAlarm error: ${e.message}", e)
        }
    }
}
