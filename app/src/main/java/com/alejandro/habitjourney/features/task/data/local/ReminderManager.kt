package com.alejandro.habitjourney.features.task.data.local

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmPermissionHelper: AlarmPermissionHelper // Usar el helper
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val prefs = context.getSharedPreferences("task_reminders", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "ReminderManager"
        private const val REQUEST_CODE_BASE = 10000
    }

    fun scheduleReminder(taskId: Long, dateTime: LocalDateTime, title: String) {
        try {
            val requestCode = (REQUEST_CODE_BASE + taskId).toInt()
            val triggerTime = dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

            // Solo programar si es en el futuro
            if (triggerTime <= System.currentTimeMillis()) {
                Log.w(TAG, "No se puede programar recordatorio en el pasado para tarea $taskId")
                return
            }

            // Verificar permisos usando el helper
            if (!alarmPermissionHelper.canScheduleExactAlarms()) {
                Log.w(TAG, "No se pueden programar alarmas exactas. Usuario debe habilitar permisos.")
                throw SecurityException("Se necesita permiso para programar alarmas exactas")
            }

            val intent = createReminderIntent(taskId, title)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Programar alarma con manejo de excepciones
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )

                // Guardar información del recordatorio
                saveReminderInfo(taskId, triggerTime, title)
                Log.d(TAG, "Recordatorio programado para tarea $taskId a las $dateTime")

            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException: No se pudo programar alarma exacta para tarea $taskId", e)
                // Intentar con alarma inexacta como fallback
                scheduleInexactAlarm(taskId, triggerTime, pendingIntent, title)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error programando recordatorio para tarea $taskId", e)
        }
    }

    private fun scheduleInexactAlarm(taskId: Long, triggerTime: Long, pendingIntent: PendingIntent, title: String) {
        try {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            saveReminderInfo(taskId, triggerTime, title)
            Log.d(TAG, "Recordatorio inexacto programado para tarea $taskId (fallback)")
        } catch (e: Exception) {
            Log.e(TAG, "Error programando recordatorio inexacto para tarea $taskId", e)
        }
    }

    fun cancelReminder(taskId: Long) {
        try {
            val requestCode = (REQUEST_CODE_BASE + taskId).toInt()
            val intent = createReminderIntent(taskId, "")
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            removeReminderInfo(taskId)

            Log.d(TAG, "Recordatorio cancelado para tarea $taskId")

        } catch (e: Exception) {
            Log.e(TAG, "Error cancelando recordatorio para tarea $taskId", e)
        }
    }

    fun updateReminder(taskId: Long, newDateTime: LocalDateTime, title: String) {
        cancelReminder(taskId)
        scheduleReminder(taskId, newDateTime, title)
    }

    fun isReminderScheduled(taskId: Long): Boolean {
        return prefs.contains("reminder_$taskId")
    }

    private fun createReminderIntent(taskId: Long, title: String): Intent {
        return Intent(context, ReminderReceiver::class.java).apply {
            putExtra("taskId", taskId)
            putExtra("title", title)
            action = "com.alejandro.habitjourney.TASK_REMINDER"
        }
    }

    private fun saveReminderInfo(taskId: Long, triggerTime: Long, title: String) {
        prefs.edit()
            .putLong("reminder_$taskId", triggerTime)
            .putString("title_$taskId", title)
            .apply()
    }

    private fun removeReminderInfo(taskId: Long) {
        prefs.edit()
            .remove("reminder_$taskId")
            .remove("title_$taskId")
            .apply()
    }
}