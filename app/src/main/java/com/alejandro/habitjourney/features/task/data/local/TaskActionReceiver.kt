package com.alejandro.habitjourney.features.task.data.local

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.utils.logging.AppLogger
import com.alejandro.habitjourney.features.task.data.worker.CompleteTaskWorker
import com.alejandro.habitjourney.features.task.data.worker.SnoozeTaskWorker
import dagger.hilt.android.AndroidEntryPoint

/**
 * [BroadcastReceiver] que maneja acciones disparadas desde las notificaciones de tareas,
 * como completar o posponer una tarea.
 *
 * Delega la lógica de negocio a [WorkManager] para que las operaciones se realicen en segundo plano.
 *
 * @see CompleteTaskWorker
 * @see SnoozeTaskWorker
 */
@AndroidEntryPoint
class TaskActionReceiver : BroadcastReceiver() {

    /**
     * Se invoca cuando el sistema envía una transmisión, específicamente para acciones de tarea.
     * Identifica la acción (`COMPLETE_TASK` o `SNOOZE_TASK`) y encola el Worker correspondiente.
     *
     * @param context El [Context] en el que se ejecuta el receptor.
     * @param intent El [Intent] que se ha transmitido, conteniendo el ID de la tarea y la acción.
     */
    @OptIn(UnstableApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("taskId", -1)

        when (intent.action) {
            "COMPLETE_TASK" -> {
                if (taskId != -1L) {
                    try {
                        val workRequest = OneTimeWorkRequestBuilder<CompleteTaskWorker>()
                            .setInputData(workDataOf(CompleteTaskWorker.KEY_TASK_ID to taskId))
                            .build()

                        val workManager = WorkManager.getInstance(context)
                        workManager.enqueue(workRequest)

                        workManager.getWorkInfoByIdLiveData(workRequest.id)
                            .observeForever { workInfo ->
                                AppLogger.d("TaskActionReceiver", "Work state: ${workInfo?.state}")
                                if (workInfo?.state?.isFinished == true) {
                                    AppLogger.d(
                                        "TaskActionReceiver",
                                        "Work output: ${workInfo.outputData}"
                                    )
                                }
                            }

                        Toast.makeText(
                            context,
                            context.getString(R.string.task_completed),
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        AppLogger.e("TaskActionReceiver", "Error al encolar el trabajo de completar tarea", e)
                    }
                }
            }

            "SNOOZE_TASK" -> {
                val snoozeMinutes = intent.getIntExtra("snoozeMinutes", 5)
                if (taskId != -1L) {
                    try {
                        val workRequest = OneTimeWorkRequestBuilder<SnoozeTaskWorker>()
                            .setInputData(
                                workDataOf(
                                    SnoozeTaskWorker.KEY_TASK_ID to taskId,
                                    SnoozeTaskWorker.KEY_SNOOZE_MINUTES to snoozeMinutes
                                )
                            )
                            .build()

                        val workManager = WorkManager.getInstance(context)
                        workManager.enqueue(workRequest)

                        Toast.makeText(
                            context,
                            context.getString(R.string.reminder_snoozed, snoozeMinutes),
                            Toast.LENGTH_SHORT
                        ).show()

                        AppLogger.d("TaskActionReceiver", "Snooze encolado para tarea $taskId, $snoozeMinutes minutos")
                    } catch (e: Exception) {
                        AppLogger.e("TaskActionReceiver", "Error al encolar el trabajo de posponer tarea", e)
                    }
                }
            }
        }
    }
}