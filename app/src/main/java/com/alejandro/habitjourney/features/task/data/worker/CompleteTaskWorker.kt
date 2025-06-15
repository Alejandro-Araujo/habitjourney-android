package com.alejandro.habitjourney.features.task.data.worker

import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alejandro.habitjourney.core.utils.logging.AppLogger
import com.alejandro.habitjourney.features.task.domain.usecase.GetTaskByIdUseCase
import com.alejandro.habitjourney.features.task.domain.usecase.ToggleTaskCompletionUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first


/**
 * [CoroutineWorker] para completar una tarea en segundo plano.
 *
 * Este worker es responsable de marcar una tarea como completada y de cancelar cualquier
 * notificación asociada. Utiliza los casos de uso para aplicar la lógica de negocio
 * y actualizar el estado de la tarea en la base de datos.
 */
@HiltWorker
class CompleteTaskWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
) : CoroutineWorker(context, params) {

    companion object {
        /**
         * Clave para el ID de la tarea pasada como dato de entrada al worker.
         */
        const val KEY_TASK_ID = "task_id"
        private const val TAG = "CompleteTaskWorker"
    }

    /**
     * Realiza el trabajo de completar la tarea.
     *
     * Recupera el ID de la tarea de los datos de entrada, cancela su notificación
     * y utiliza [ToggleTaskCompletionUseCase] para marcar la tarea como completada.
     *
     * @return [Result.success] si la tarea se completa exitosamente, [Result.failure] en caso de error.
     */
    override suspend fun doWork(): Result {
        return try {
            val taskId = inputData.getLong(KEY_TASK_ID, -1L)

            if (taskId == -1L) {
                AppLogger.e(TAG, "ID de tarea no válido.")
                return Result.failure()
            }
            val task = getTaskByIdUseCase(taskId).first()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(taskId.toInt())
            AppLogger.d(TAG, "Notificación cancelada para ID: $taskId")

            if (task != null) {
                if (!task.isCompleted) {
                    toggleTaskCompletionUseCase(taskId = task.id, isCompleted = true)
                    AppLogger.d(TAG, "Tarea completada exitosamente a través del caso de uso: $taskId")
                } else {
                    AppLogger.d(TAG, "La tarea ya estaba completada, no se realiza ninguna acción: $taskId")
                }
            }

            Result.success()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error al completar tarea", e)
            Result.failure()
        }
    }
}