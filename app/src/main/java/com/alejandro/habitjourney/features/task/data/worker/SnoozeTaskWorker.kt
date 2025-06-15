package com.alejandro.habitjourney.features.task.data.worker

import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alejandro.habitjourney.core.utils.logging.AppLogger
import com.alejandro.habitjourney.features.task.data.local.ReminderManager
import com.alejandro.habitjourney.features.task.domain.usecase.GetTaskByIdUseCase
import com.alejandro.habitjourney.features.task.domain.usecase.UpdateTaskUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*


/**
 * [CoroutineWorker] para posponer un recordatorio de tarea.
 *
 * Este worker se activa cuando el usuario elige posponer un recordatorio desde una notificación.
 * Se encarga de calcular una nueva hora para el recordatorio, actualizar la tarea en la base de datos
 * y reprogramar el recordatorio.
 */
@HiltWorker
class SnoozeTaskWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val reminderManager: ReminderManager
) : CoroutineWorker(context, params) {

    companion object {
        /**
         * Clave para el ID de la tarea pasada como dato de entrada al worker.
         */
        const val KEY_TASK_ID = "task_id"
        /**
         * Clave para la cantidad de minutos que se debe posponer la tarea.
         */
        const val KEY_SNOOZE_MINUTES = "snooze_minutes"
        private const val TAG = "SnoozeTaskWorker"
    }

    /**
     * Realiza el trabajo de posponer un recordatorio de tarea.
     *
     * Recupera el ID de la tarea y los minutos de posposición de los datos de entrada.
     * Cancela la notificación actual, calcula la nueva hora del recordatorio,
     * actualiza la tarea en la base de datos y reprograma el recordatorio.
     *
     * @return [Result.success] si el recordatorio se pospone exitosamente, [Result.failure] en caso de error.
     */
    override suspend fun doWork(): Result {
        return try {
            val taskId = inputData.getLong(KEY_TASK_ID, -1L)
            val snoozeMinutes = inputData.getInt(KEY_SNOOZE_MINUTES, 5)

            if (taskId == -1L) {
                AppLogger.e(TAG, "ID de tarea no válido.")
                return Result.failure()
            }

            AppLogger.d(TAG, "Procesando posposición para tarea ID: $taskId, $snoozeMinutes minutos")

            val task = getTaskByIdUseCase(taskId).first()
            if (task == null) {
                AppLogger.e(TAG, "Tarea no encontrada: $taskId")
                return Result.failure()
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(taskId.toInt())
            AppLogger.d(TAG, "Notificación cancelada para tarea: $taskId")

            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val newReminderTime = now.plus(snoozeMinutes, DateTimeUnit.MINUTE)

            AppLogger.d(TAG, "Nueva hora de recordatorio: $newReminderTime")

            val updatedTask = task.copy(
                reminderDateTime = newReminderTime,
                isReminderSet = true
            )
            updateTaskUseCase(updatedTask)
            AppLogger.d(TAG, "Tarea actualizada con nueva hora de recordatorio.")

            reminderManager.scheduleReminder(
                taskId = task.id,
                dateTime = newReminderTime,
                title = task.title
            )
            AppLogger.d(TAG, "Recordatorio reprogramado para tarea: $taskId en $newReminderTime")

            Result.success()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error al posponer tarea", e)
            Result.failure()
        }
    }

    /**
     * Función de extensión para sumar un valor a una [LocalDateTime] utilizando una unidad de tiempo basada.
     * Convierte la [LocalDateTime] a [Instant], suma el valor y luego la reconvierte a [LocalDateTime].
     *
     * @receiver La [LocalDateTime] a la que se sumará el valor.
     * @param value El valor numérico a sumar.
     * @param unit La unidad de tiempo ([DateTimeUnit.TimeBased]) a sumar (ej. [DateTimeUnit.MINUTE], [DateTimeUnit.HOUR]).
     * @return Una nueva [LocalDateTime] con el valor sumado.
     */
    private fun LocalDateTime.plus(value: Int, unit: DateTimeUnit.TimeBased): LocalDateTime {
        return this.toInstant(TimeZone.currentSystemDefault())
            .plus(value, unit, TimeZone.currentSystemDefault())
            .toLocalDateTime(TimeZone.currentSystemDefault())
    }
}