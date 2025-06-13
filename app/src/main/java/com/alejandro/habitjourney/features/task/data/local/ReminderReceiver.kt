package com.alejandro.habitjourney.features.task.data.local

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.alejandro.habitjourney.MainActivity
import com.alejandro.habitjourney.R

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "task_reminders"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("taskId", -1)
        val title = intent.getStringExtra("title") ?: context.getString(R.string.task_reminder_default)

        if (taskId != -1L) {
            showNotification(context, taskId, title)
        }
    }

    private fun showNotification(context: Context, taskId: Long, title: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal de notificación
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

        // Crear notificación con acciones
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.task_reminder_title))
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            // Acción de completar
            .addAction(
                R.drawable.ic_check,
                context.getString(R.string.complete_action),
                createCompleteTaskIntent(context, taskId)
            )

        // Agregar acciones de snooze solo si hay espacio (máximo 3 acciones recomendado)
        // Opción 1: 3 acciones de snooze
        notificationBuilder
            .addAction(
                R.drawable.ic_snooze,
                context.getString(R.string.snooze_5_min),
                createSnoozeIntent(context, taskId, 5)
            )
            .addAction(
                R.drawable.ic_snooze,
                context.getString(R.string.snooze_15_min),
                createSnoozeIntent(context, taskId, 15)
            )

        // Si prefieres incluir más opciones, puedes comentar las líneas anteriores
        // y descomentar esta para una sola acción de snooze por defecto:
        /*
        .addAction(
            R.drawable.ic_snooze,
            context.getString(R.string.snooze_default),
            createSnoozeIntent(context, taskId, 10)
        )
        */

        notificationManager.notify(taskId.toInt(), notificationBuilder.build())
    }

    private fun createNotificationChannel(context: Context, notificationManager: NotificationManager) {
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

    private fun createSnoozeIntent(context: Context, taskId: Long, minutes: Int): PendingIntent {
        val snoozeIntent = Intent(context, TaskActionReceiver::class.java).apply {
            action = "SNOOZE_TASK"
            putExtra("taskId", taskId)
            putExtra("snoozeMinutes", minutes)
        }

        return PendingIntent.getBroadcast(
            context,
            (taskId + 60000 + minutes).toInt(), // ID único para cada acción de snooze
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}