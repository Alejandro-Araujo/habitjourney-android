package com.alejandro.habitjourney.features.task.data.worker

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alejandro.habitjourney.features.task.data.local.ReminderManager
import com.alejandro.habitjourney.features.task.domain.usecase.GetTaskByIdUseCase
import com.alejandro.habitjourney.features.task.domain.usecase.UpdateTaskUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*

@HiltWorker
class SnoozeTaskWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val reminderManager: ReminderManager
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_TASK_ID = "task_id"
        const val KEY_SNOOZE_MINUTES = "snooze_minutes"
        const val TAG = "SnoozeTaskWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            val taskId = inputData.getLong(KEY_TASK_ID, -1L)
            val snoozeMinutes = inputData.getInt(KEY_SNOOZE_MINUTES, 5)

            if (taskId == -1L) {
                Log.e(TAG, "Invalid task ID")
                return Result.failure()
            }

            Log.d(TAG, "Processing snooze for task $taskId, $snoozeMinutes minutes")

            // Obtener la tarea
            val task = getTaskByIdUseCase(taskId).first()
            if (task == null) {
                Log.e(TAG, "Task not found: $taskId")
                return Result.failure()
            }

            // Cancelar la notificación actual
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(taskId.toInt())
            Log.d(TAG, "Notification cancelled for task: $taskId")

            // Calcular nueva hora de recordatorio
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val newReminderTime = now.plus(snoozeMinutes, DateTimeUnit.MINUTE)

            Log.d(TAG, "New reminder time: $newReminderTime")

            // Actualizar la tarea con el nuevo recordatorio
            val updatedTask = task.copy(
                reminderDateTime = newReminderTime,
                isReminderSet = true
            )
            updateTaskUseCase(updatedTask)
            Log.d(TAG, "Task updated with new reminder time")

            // Reprogramar el recordatorio
            reminderManager.scheduleReminder(
                taskId = task.id,
                dateTime = newReminderTime,
                title = task.title
            )
            Log.d(TAG, "Reminder rescheduled for task: $taskId at $newReminderTime")

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error snoozing task", e)
            Result.failure()
        }
    }

    private fun LocalDateTime.plus(value: Int, unit: DateTimeUnit.TimeBased): LocalDateTime {
        return this.toInstant(TimeZone.currentSystemDefault())
            .plus(value, unit, TimeZone.currentSystemDefault())
            .toLocalDateTime(TimeZone.currentSystemDefault())
    }
}