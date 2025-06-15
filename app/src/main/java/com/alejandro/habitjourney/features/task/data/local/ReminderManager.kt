package com.alejandro.habitjourney.features.task.data.local

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.alejandro.habitjourney.core.utils.logging.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestiona la programación, cancelación y actualización de recordatorios de tareas
 * utilizando [AlarmManager] y [PendingIntent].
 *
 * Esta clase también se encarga de persistir información básica sobre los recordatorios programados
 * para su recuperación y verificación, así como de interactuar con [AlarmPermissionHelper]
 * para asegurar que los permisos necesarios estén concedidos.
 *
 * @property context El contexto de la aplicación, inyectado por Hilt.
 * @property alarmPermissionHelper El asistente para verificar y solicitar permisos de alarma.
 */
@Singleton
class ReminderManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmPermissionHelper: AlarmPermissionHelper
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val prefs = context.getSharedPreferences("task_reminders", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "ReminderManager"
        private const val REQUEST_CODE_BASE = 10000
    }

    /**
     * Programa un recordatorio para una tarea específica en una fecha y hora dadas.
     *
     * Utiliza `AlarmManager.setExactAndAllowWhileIdle` para alarmas precisas si los permisos lo permiten.
     * Si no se pueden programar alarmas exactas, intenta con una alarma inexacta como fallback.
     * La información del recordatorio se guarda localmente para su seguimiento.
     *
     * @param taskId El ID único de la tarea asociada al recordatorio.
     * @param dateTime La [LocalDateTime] en la que debe activarse el recordatorio.
     * @param title El título de la tarea, usado para la notificación.
     * @throws SecurityException Si no se tiene el permiso necesario para programar alarmas exactas y no se puede usar el fallback.
     */
    fun scheduleReminder(taskId: Long, dateTime: LocalDateTime, title: String) {
        try {
            val requestCode = (REQUEST_CODE_BASE + taskId).toInt()
            val triggerTime = dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

            if (triggerTime <= System.currentTimeMillis()) {
                AppLogger.w(TAG, "No se puede programar recordatorio en el pasado para tarea $taskId")
                return
            }

            if (!alarmPermissionHelper.canScheduleExactAlarms()) {
                AppLogger.w(TAG, "No se pueden programar alarmas exactas. Usuario debe habilitar permisos.")
                throw SecurityException("Se necesita permiso para programar alarmas exactas")
            }

            val intent = createReminderIntent(taskId, title)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )

                saveReminderInfo(taskId, triggerTime, title)
                AppLogger.d(TAG, "Recordatorio programado para tarea $taskId a las $dateTime")

            } catch (e: SecurityException) {
                AppLogger.e(TAG, "SecurityException: No se pudo programar alarma exacta para tarea $taskId", e)
                scheduleInexactAlarm(taskId, triggerTime, pendingIntent, title)
            }

        } catch (e: Exception) {
            AppLogger.e(TAG, "Error programando recordatorio para tarea $taskId", e)
        }
    }

    /**
     * Programa un recordatorio utilizando una alarma inexacta como fallback.
     * Se usa cuando `setExactAndAllowWhileIdle` falla debido a falta de permisos.
     *
     * @param taskId El ID único de la tarea.
     * @param triggerTime El tiempo en milisegundos cuando la alarma debe activarse.
     * @param pendingIntent El [PendingIntent] a activar cuando la alarma se dispara.
     * @param title El título de la tarea.
     */
    private fun scheduleInexactAlarm(taskId: Long, triggerTime: Long, pendingIntent: PendingIntent, title: String) {
        try {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            saveReminderInfo(taskId, triggerTime, title)
            AppLogger.d(TAG, "Recordatorio inexacto programado para tarea $taskId (fallback)")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error programando recordatorio inexacto para tarea $taskId", e)
        }
    }

    /**
     * Cancela un recordatorio previamente programado para una tarea específica.
     * También elimina la información del recordatorio de las preferencias locales.
     *
     * @param taskId El ID único de la tarea cuyo recordatorio se desea cancelar.
     */
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

            AppLogger.d(TAG, "Recordatorio cancelado para tarea $taskId")

        } catch (e: Exception) {
            AppLogger.e(TAG, "Error cancelando recordatorio para tarea $taskId", e)
        }
    }

    /**
     * Actualiza un recordatorio existente.
     * Internamente, cancela el recordatorio anterior y programa uno nuevo con la nueva información.
     *
     * @param taskId El ID único de la tarea cuyo recordatorio se desea actualizar.
     * @param newDateTime La nueva [LocalDateTime] para el recordatorio.
     * @param title El nuevo título para el recordatorio.
     */
    fun updateReminder(taskId: Long, newDateTime: LocalDateTime, title: String) {
        cancelReminder(taskId)
        scheduleReminder(taskId, newDateTime, title)
    }

    /**
     * Verifica si existe un recordatorio programado para una tarea específica en las preferencias locales.
     *
     * @param taskId El ID único de la tarea a verificar.
     * @return `true` si hay información de un recordatorio guardada para esa tarea, `false` en caso contrario.
     */
    fun isReminderScheduled(taskId: Long): Boolean {
        return prefs.contains("reminder_$taskId")
    }

    /**
     * Crea un [Intent] que será disparado cuando el recordatorio se active.
     * El Intent está destinado a ser recibido por [ReminderReceiver].
     *
     * @param taskId El ID de la tarea a incluir en el Intent.
     * @param title El título de la tarea a incluir en el Intent.
     * @return Un [Intent] configurado para el recordatorio.
     */
    private fun createReminderIntent(taskId: Long, title: String): Intent {
        return Intent(context, ReminderReceiver::class.java).apply {
            putExtra("taskId", taskId)
            putExtra("title", title)
            action = "com.alejandro.habitjourney.TASK_REMINDER"
        }
    }

    /**
     * Guarda la información de un recordatorio programado en las preferencias compartidas.
     *
     * @param taskId El ID de la tarea.
     * @param triggerTime El tiempo en milisegundos cuando se programó el recordatorio.
     * @param title El título de la tarea asociado.
     */
    private fun saveReminderInfo(taskId: Long, triggerTime: Long, title: String) {
        prefs.edit()
            .putLong("reminder_$taskId", triggerTime)
            .putString("title_$taskId", title)
            .apply()
    }

    /**
     * Elimina la información de un recordatorio de las preferencias compartidas.
     *
     * @param taskId El ID de la tarea cuyo recordatorio se va a eliminar de las preferencias.
     */
    private fun removeReminderInfo(taskId: Long) {
        prefs.edit()
            .remove("reminder_$taskId")
            .remove("title_$taskId")
            .apply()
    }
}