package com.alejandro.habitjourney.features.task.data.worker

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alejandro.habitjourney.features.task.domain.usecase.GetTaskByIdUseCase
import com.alejandro.habitjourney.features.task.domain.usecase.ToggleTaskCompletionUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class CompleteTaskWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_TASK_ID = "task_id"
        const val TAG = "CompleteTaskWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            val taskId = inputData.getLong(KEY_TASK_ID, -1L)

            if (taskId == -1L) {
                Log.e(TAG, "Invalid task ID")
                return Result.failure()
            }
            val task = getTaskByIdUseCase(taskId).first()
            // Cancelar la notificación
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(taskId.toInt())
            Log.d(TAG, "Notification cancelled for ID: $taskId")

            // Completar la tarea
            if (task != null) {
                if (!task.isCompleted) {
                    // El caso de uso se encarga de la lógica de fechas y de llamar al repositorio.
                    toggleTaskCompletionUseCase(taskId = task.id, isCompleted = true)
                    Log.d(TAG, "Tarea completada exitosamente a través del caso de uso: $taskId")
                } else {
                    Log.d(TAG, "La tarea ya estaba completada, no se hace nada: $taskId")
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error completing task", e)
            Result.failure()
        }
    }
}