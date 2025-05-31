package com.alejandro.habitjourney.features.task.data.local

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.alejandro.habitjourney.MainActivity
import com.alejandro.habitjourney.R

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "task_reminders"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("taskId", -1)
        val title = intent.getStringExtra("title") ?:context.getString(R.string.task_reminder_default)

        if (taskId != -1L) {
            showNotification(context, taskId, title)
        }
    }

    private fun showNotification(context: Context, taskId: Long, title: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal de notificación para Android 8.0+
        createNotificationChannel(context, notificationManager)

        // Intent para abrir la app en la tarea específica
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("taskId", taskId)
            putExtra("openTaskDetail", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Crear notificación
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.task_reminder_title))
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_check,
                context.getString(R.string.complete_action),
                createCompleteTaskIntent(context, taskId)
            )
            .build()

        notificationManager.notify(taskId.toInt(), notification)
    }

    private fun createNotificationChannel(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.task_reminders_channel),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.task_reminders_channel_desc)
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createCompleteTaskIntent(context: Context, taskId: Long): PendingIntent {
        val completeIntent = Intent(context, TaskActionReceiver::class.java).apply {
            action = "COMPLETE_TASK"
            putExtra("taskId", taskId)
        }

        return PendingIntent.getBroadcast(
            context,
            (taskId + 50000).toInt(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}