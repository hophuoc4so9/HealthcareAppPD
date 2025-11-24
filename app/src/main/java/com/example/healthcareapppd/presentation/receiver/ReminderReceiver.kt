package com.example.healthcareapppd.presentation.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.util.Log
import android.widget.Toast
import com.example.healthcareapppd.R
import com.example.healthcareapppd.MainActivity

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "onReceive called at " +
            java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()))
        val title = intent.getStringExtra("title") ?: "Nhắc nhở"
        val description = intent.getStringExtra("description") ?: "Đến giờ nhắc nhở!"
        val channelId = "reminder_channel"

        // Tạo notification channel cho Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Nhắc nhở",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Kênh thông báo nhắc nhở"
            channel.enableVibration(true)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        // Intent để mở app khi tap notification
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(title)
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // <-- Thêm dòng này để chuông báo luôn phát
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 500, 250, 500))

        val notificationManagerCompat = NotificationManagerCompat.from(context)
        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Log.d("ReminderReceiver", "Sending notification: $title - $description")
            notificationManagerCompat.notify(System.currentTimeMillis().toInt(), builder.build())
        } else {
            Log.w("ReminderReceiver", "No POST_NOTIFICATIONS permission")
            Toast.makeText(context, "Ứng dụng chưa được cấp quyền thông báo!", Toast.LENGTH_LONG).show()
        }
    }
}