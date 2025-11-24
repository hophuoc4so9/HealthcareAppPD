package com.example.healthcareapppd.presentation.ui.user.reminder

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.healthcareapppd.R
import com.example.healthcareapppd.domain.usecase.reminder.CreateReminderUseCase
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.*
import android.util.Log
import android.provider.Settings

class AddReminderFragment : Fragment() {
    
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var editLabel: TextInputEditText
    private lateinit var editDescription: TextInputEditText
    private lateinit var switchActive: SwitchMaterial
    private lateinit var timePicker: TimePicker
    private lateinit var spinnerType: Spinner
    private lateinit var progressBar: ProgressBar
    private lateinit var checkboxMon: CheckBox
    private lateinit var checkboxTue: CheckBox
    private lateinit var checkboxWed: CheckBox
    private lateinit var checkboxThu: CheckBox
    private lateinit var checkboxFri: CheckBox
    private lateinit var checkboxSat: CheckBox
    private lateinit var checkboxSun: CheckBox
    
    private val createReminderUseCase = CreateReminderUseCase()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.reminder_fragment_add_item, container, false)
        
        initViews(view)
        setupReminderTypeSpinner()
        setupClickListeners()
        
        return view
    }

    private fun initViews(view: View) {
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
    }

    private fun setupReminderTypeSpinner() {
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
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            createReminder()
        }

        btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun createReminder() {
        val title = editLabel.text?.toString()?.trim()
        if (title.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show()
            return
        }

        val hour = timePicker.hour
        val minute = timePicker.minute
        val description = editDescription.text?.toString()?.trim()
        
        val reminderTypes = arrayOf(
            "medication" to "Uống thuốc",
            "sleep" to "Giấc ngủ",
            "appointment" to "Lịch hẹn",
            "general" to "Chung"
        )
        val reminderTypeValue = reminderTypes[spinnerType.selectedItemPosition].first
        
        // Lấy các ngày được chọn (1=Mon, 2=Tue,..., 0=Sun theo cron)
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
            // Không chọn ngày nào -> nhắc 1 lần
            oneTimeAt = calculateOneTimeAt(hour, minute)
            cronExpression = null
        } else {
            // Có chọn ngày -> tạo cron expression
            val daysString = daysChecked.sorted().joinToString(",")
            cronExpression = "$minute $hour * * $daysString"
        }

        progressBar.visibility = View.VISIBLE
        btnSave.isEnabled = false

        lifecycleScope.launch {
            createReminderUseCase(
                context = requireContext(),
                title = title,
                description = description,
                reminderType = reminderTypeValue,
                cronExpression = cronExpression,
                oneTimeAt = oneTimeAt,
                timezoneName = "Asia/Ho_Chi_Minh"
            ).fold(
                onSuccess = {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Đã tạo nhắc nhở", Toast.LENGTH_SHORT).show()
                    
                    // Schedule local notification nếu cần
                    if (oneTimeAt != null) {
                        scheduleLocalNotification(title, description, oneTimeAt)
                    } else if (cronExpression != null) {
                        scheduleRecurringNotification(title, description, cronExpression)
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

    private fun calculateOneTimeAt(hour: Int, minute: Int): String {
        val now = Calendar.getInstance()
        val reminderTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // Nếu thời gian đã qua hôm nay, chuyển sang ngày mai
        if (reminderTime.timeInMillis <= now.timeInMillis) {
            reminderTime.add(Calendar.DAY_OF_MONTH, 1)
        }
        // Format: yyyy-MM-dd'T'HH:mm:ss.SSS'Z' (UTC)
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(reminderTime.time)
    }

    private fun scheduleLocalNotification(title: String, description: String?, oneTimeAt: String) {
        try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
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
                    Log.d("AddReminderFragment", "Đặt alarm lặp lại: $title, day=$day, time=$nextTriggerTime")
                    scheduleReminderAlarm(title, description ?: "", nextTriggerTime)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateNextDayTriggerTime(hour: Int, minute: Int, cronDay: Int): Long {
        val now = Calendar.getInstance()
        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val calendarDay = if (cronDay == 0) Calendar.SUNDAY else cronDay + 1
        for (i in 0..7) {
            if (trigger.get(Calendar.DAY_OF_WEEK) == calendarDay && trigger.timeInMillis > now.timeInMillis) {
                return trigger.timeInMillis
            }
            trigger.add(Calendar.DAY_OF_MONTH, 1)
        }
        return trigger.timeInMillis
    }

    private fun scheduleReminderAlarm(title: String, description: String, triggerTimeMillis: Long) {
        Log.d("AddReminderFragment", "scheduleReminderAlarm: $title, $description, triggerTimeMillis=$triggerTimeMillis, time=" +
            java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(triggerTimeMillis)))
        if (triggerTimeMillis < System.currentTimeMillis()) {
            Log.w("AddReminderFragment", "Không đặt alarm vì thời gian đã qua: $triggerTimeMillis")
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
                    Log.e("AddReminderFragment", "scheduleReminderAlarm error: ${e.message}", e)
                    Toast.makeText(requireContext(),
                        "Bạn cần cấp quyền Báo thức chính xác cho ứng dụng trong Cài đặt > Ứng dụng > HealthcareAppPD > Quyền đặc biệt > Báo thức chính xác.",
                        Toast.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
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
            Log.e("AddReminderFragment", "scheduleReminderAlarm error: ${e.message}", e)
        }
    }
}