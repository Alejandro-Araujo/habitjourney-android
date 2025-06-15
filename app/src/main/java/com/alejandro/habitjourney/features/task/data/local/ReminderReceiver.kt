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

/**
 * [BroadcastReceiver] responsable de recibir las transmisiones de recordatorios
 * y mostrar notificaciones al usuario.
 *
 * Cuando se activa un recordatorio programado, este receptor crea y muestra
 * una notificación que permite al usuario interactuar con la tarea (ej. completarla, posponerla).
 */
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "task_reminders"
    }

    /**
     * Se invoca cuando el sistema envía una transmisión para un recordatorio de tarea.
     * Extrae el ID de la tarea y el título del Intent y muestra una notificación.
     *
     * @param context El [Context] en el que se ejecuta el receptor.
     * @param intent El [Intent] que se ha transmitido, conteniendo los detalles del recordatorio.
     */
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("taskId", -1)
        val title = intent.getStringExtra("title") ?: context.getString(R.string.task_reminder_default)

        if (taskId != -1L) {
            showNotification(context, taskId, title)
        }
    }

    /**
     * Muestra una notificación al usuario para el recordatorio de la tarea.
     *
     * Configura el contenido de la notificación, las acciones (completar, posponer)
     * y el comportamiento al hacer clic en ella (abrir la app en los detalles de la tarea).
     *
     * @param context El [Context] de la aplicación.
     * @param taskId El ID de la tarea asociada a la notificación.
     * @param title El título de la notificación, generalmente el título de la tarea.
     */
    private fun showNotification(context: Context, taskId: Long, title: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel(context, notificationManager)

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

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
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

        notificationManager.notify(taskId.toInt(), notificationBuilder.build())
    }

    /**
     * Crea un canal de notificación si aún no existe.
     * Es necesario para las notificaciones en Android 8.0 (API 26) y superiores.
     *
     * @param context El [Context] de la aplicación.
     * @param notificationManager El [NotificationManager] del sistema.
     */
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

    /**
     * Crea un [PendingIntent] para la acción de completar una tarea desde la notificación.
     * Este Intent será recibido por [TaskActionReceiver].
     *
     * @param context El [Context] de la aplicación.
     * @param taskId El ID de la tarea a completar.
     * @return Un [PendingIntent] para la acción de completar.
     */
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

    /**
     * Crea un [PendingIntent] para la acción de posponer una tarea desde la notificación.
     * Este Intent será recibido por [TaskActionReceiver].
     *
     * @param context El [Context] de la aplicación.
     * @param taskId El ID de la tarea a posponer.
     * @param minutes El número de minutos que se desea posponer la tarea.
     * @return Un [PendingIntent] para la acción de posponer.
     */
    private fun createSnoozeIntent(context: Context, taskId: Long, minutes: Int): PendingIntent {
        val snoozeIntent = Intent(context, TaskActionReceiver::class.java).apply {
            action = "SNOOZE_TASK"
            putExtra("taskId", taskId)
            putExtra("snoozeMinutes", minutes)
        }

        return PendingIntent.getBroadcast(
            context,
            (taskId + 60000 + minutes).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}