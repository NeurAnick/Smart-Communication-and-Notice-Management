package com.example.smartcommunicationandnoticemanagement.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.smartcommunicationandnoticemanagement.MainActivity

object NotificationHelper {

    const val CHANNEL_NOTICES = "CHANNEL_NOTICES"
    const val CHANNEL_URGENT = "CHANNEL_URGENT"
    const val CHANNEL_MESSAGES = "CHANNEL_MESSAGES"
    const val CHANNEL_SYSTEM = "CHANNEL_SYSTEM"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val noticeChannel = NotificationChannel(
                CHANNEL_NOTICES,
                "Normal Notices",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "General and important notices"
                lightColor = 0xFF0288D1.toInt()
            }

            val urgentChannel = NotificationChannel(
                CHANNEL_URGENT,
                "Urgent Notices",
                NotificationManager.IMPORTANCE_MAX
            ).apply { description = "Crucial and urgent announcements requiring immediate attention" }

            val msgChannel = NotificationChannel(
                CHANNEL_MESSAGES,
                "Direct Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Direct communications"
                val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(sound, attrs)
            }
            val systemChannel = NotificationChannel(
                CHANNEL_SYSTEM,
                "System Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Approval and login alerts" }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(noticeChannel)
            notificationManager.createNotificationChannel(urgentChannel)
            notificationManager.createNotificationChannel(msgChannel)
            notificationManager.createNotificationChannel(systemChannel)
        }
    }

    fun showNoticeNotification(context: Context, title: String, body: String, noticeId: String?) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            noticeId?.let { putExtra("noticeId", it) }
            putExtra("type", "notice")
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_NOTICES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    fun showUrgentNotification(context: Context, title: String, body: String, noticeId: String?) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            noticeId?.let { putExtra("noticeId", it) }
            putExtra("type", "notice")
        }
        val pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_URGENT)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("URGENT: $title")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    fun showMessageNotification(context: Context, senderName: String, subject: String, roomId: String? = null) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("type", "message")
            roomId?.let { putExtra("roomId", it) }
        }
        val pendingIntent = PendingIntent.getActivity(context, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_MESSAGES)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("Message from $senderName")
            .setContentText(subject)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    fun showSystemNotification(context: Context, title: String, body: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("type", "approval")
        }
        val pendingIntent = PendingIntent.getActivity(context, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(context, CHANNEL_SYSTEM)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
