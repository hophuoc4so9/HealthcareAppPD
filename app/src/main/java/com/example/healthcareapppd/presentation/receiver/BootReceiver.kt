package com.example.healthcareapppd.presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.healthcareapppd.domain.usecase.reminder.GetRemindersUseCase
import java.util.*
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleReminders(context)
        }
    }

    private fun rescheduleReminders(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val getRemindersUseCase = GetRemindersUseCase()
                val result = getRemindersUseCase(context)
                
                result.getOrNull()?.forEach { reminder ->
                    if (reminder.isActive) {
                        if (reminder.cronExpression != null) {
                            scheduleRecurringReminder(context, reminder)
                        } else if (reminder.oneTimeAt != null) {
                            scheduleOneTimeReminder(context, reminder)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun scheduleRecurringReminder(context: Context, reminder: com.example.healthcareapppd.data.api.model.Reminder) {
        try {
            val parts = reminder.cronExpression?.split(" ") ?: return
            if (parts.size >= 5) {
                val minute = parts[0].toIntOrNull() ?: 0
                val hour = parts[1].toIntOrNull() ?: 0
                val days = parts[4].split(",").mapNotNull { it.toIntOrNull() }
                // Đặt alarm cho từng ngày trong tuần
                days.forEach { day ->
                    val nextTriggerTime = calculateNextDayTriggerTime(hour, minute, day)
                    Log.d("BootReceiver", "Đặt alarm lặp lại: ${reminder.title}, day=$day, time=$nextTriggerTime")
                    scheduleAlarm(context, reminder.id + "_$day", reminder.title, reminder.description, nextTriggerTime)
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

    private fun scheduleOneTimeReminder(context: Context, reminder: com.example.healthcareapppd.data.api.model.Reminder) {
        try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(reminder.oneTimeAt ?: return)
            date?.let {
                if (it.time > System.currentTimeMillis()) {
                    scheduleAlarm(context, reminder.id, reminder.title, reminder.description, it.time)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateNextTriggerTime(hour: Int, minute: Int, days: List<Int>): Long {
        val now = Calendar.getInstance()
        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        if (days.isEmpty()) {
            if (trigger.timeInMillis <= now.timeInMillis) {
                trigger.add(Calendar.DAY_OF_MONTH, 1)
            }
            return trigger.timeInMillis
        }
        
        val targetDays = days.map { cronDay ->
            if (cronDay == 0) Calendar.SUNDAY else cronDay + 1
        }.sorted()
        
        for (i in 0..7) {
            val checkDay = trigger.get(Calendar.DAY_OF_WEEK)
            if (targetDays.contains(checkDay) && trigger.timeInMillis > now.timeInMillis) {
                break
            }
            trigger.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        return trigger.timeInMillis
    }

    private fun scheduleAlarm(context: Context, reminderId: String, title: String, description: String?, triggerTimeMillis: Long) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("description", description ?: "")
        }
        
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
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
    }
}