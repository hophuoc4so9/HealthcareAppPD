package com.example.healthcareapppd.utils

/**
 * Utility class for generating cron expressions for reminders
 * Cron format: "minute hour day month dayOfWeek"
 */
object CronExpressionHelper {
    
    /**
     * Generate cron expression for daily reminder at specific time
     * @param hour Hour in 24-hour format (0-23)
     * @param minute Minute (0-59)
     * @return Cron expression like "0 8 * * *" for 8:00 AM daily
     */
    fun daily(hour: Int, minute: Int): String {
        return "$minute $hour * * *"
    }
    
    /**
     * Generate cron expression for multiple times per day
     * @param hours List of hours in 24-hour format
     * @param minute Minute for all occurrences
     * @return Cron expression like "0 8,20 * * *" for 8:00 AM and 8:00 PM
     */
    fun dailyMultipleTimes(hours: List<Int>, minute: Int): String {
        val hoursStr = hours.joinToString(",")
        return "$minute $hoursStr * * *"
    }
    
    /**
     * Generate cron expression for weekdays only (Monday-Friday)
     * @param hour Hour in 24-hour format (0-23)
     * @param minute Minute (0-59)
     * @return Cron expression like "0 9 * * 1-5" for 9:00 AM on weekdays
     */
    fun weekdays(hour: Int, minute: Int): String {
        return "$minute $hour * * 1-5"
    }
    
    /**
     * Generate cron expression for weekends only (Saturday-Sunday)
     * @param hour Hour in 24-hour format (0-23)
     * @param minute Minute (0-59)
     * @return Cron expression like "0 9 * * 6,0" for 9:00 AM on weekends
     */
    fun weekends(hour: Int, minute: Int): String {
        return "$minute $hour * * 6,0"
    }
    
    /**
     * Generate cron expression for specific days of week
     * @param daysOfWeek List of days (0=Sunday, 1=Monday, ..., 6=Saturday)
     * @param hour Hour in 24-hour format (0-23)
     * @param minute Minute (0-59)
     * @return Cron expression for specific days
     */
    fun specificDays(daysOfWeek: List<Int>, hour: Int, minute: Int): String {
        val daysStr = daysOfWeek.sorted().joinToString(",")
        return "$minute $hour * * $daysStr"
    }
    
    /**
     * Generate cron expression for every N hours
     * @param everyNHours Interval in hours (e.g., 2 for every 2 hours)
     * @param startHour Starting hour (default 0)
     * @param minute Minute (default 0)
     */
    fun everyNHours(everyNHours: Int, startHour: Int = 0, minute: Int = 0): String {
        return if (startHour == 0) {
            "$minute */$everyNHours * * *"
        } else {
            "$minute $startHour-23/$everyNHours * * *"
        }
    }
    
    /**
     * Parse cron expression to human-readable format (Vietnamese)
     * @param cronExpression Cron expression to parse
     * @return Human-readable description
     */
    fun parseCronToReadable(cronExpression: String): String {
        val parts = cronExpression.split(" ")
        if (parts.size < 5) return cronExpression
        
        val minute = parts[0]
        val hour = parts[1]
        val dayOfMonth = parts[2]
        val month = parts[3]
        val dayOfWeek = parts[4]
        
        return when {
            // Daily at specific time
            dayOfWeek == "*" && dayOfMonth == "*" && month == "*" -> {
                val hourStr = hour.padStart(2, '0')
                val minuteStr = minute.padStart(2, '0')
                "Hàng ngày lúc $hourStr:$minuteStr"
            }
            // Weekdays
            dayOfWeek == "1-5" -> {
                val hourStr = hour.padStart(2, '0')
                val minuteStr = minute.padStart(2, '0')
                "Thứ 2-6 lúc $hourStr:$minuteStr"
            }
            // Weekends
            dayOfWeek == "6,0" || dayOfWeek == "0,6" -> {
                val hourStr = hour.padStart(2, '0')
                val minuteStr = minute.padStart(2, '0')
                "Cuối tuần lúc $hourStr:$minuteStr"
            }
            // Multiple times per day
            hour.contains(",") -> {
                val hours = hour.split(",")
                val times = hours.map { h -> "${h.padStart(2, '0')}:${minute.padStart(2, '0')}" }
                "Hàng ngày lúc ${times.joinToString(", ")}"
            }
            // Every N hours
            hour.startsWith("*/") -> {
                val interval = hour.substring(2)
                "Mỗi $interval giờ"
            }
            else -> cronExpression
        }
    }
    
    /**
     * Format ISO 8601 datetime for one-time reminder
     * @param year Year
     * @param month Month (1-12)
     * @param day Day (1-31)
     * @param hour Hour (0-23)
     * @param minute Minute (0-59)
     * @return ISO 8601 formatted string
     */
    fun formatOneTimeReminder(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int
    ): String {
        return String.format(
            "%04d-%02d-%02dT%02d:%02d:00Z",
            year, month, day, hour, minute
        )
    }
}
