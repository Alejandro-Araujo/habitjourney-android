package com.alejandro.habitjourney.features.task.data.local

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.features.task.data.worker.CompleteTaskWorker
import com.alejandro.habitjourney.features.task.data.worker.SnoozeTaskWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskActionReceiver : BroadcastReceiver() {

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

                        // Observar el estado del trabajo para debug
                        workManager.getWorkInfoByIdLiveData(workRequest.id)
                            .observeForever { workInfo ->
                                Log.d("TaskActionReceiver", "Work state: ${workInfo?.state}")
                                if (workInfo?.state?.isFinished == true) {
                                    Log.d(
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
                        Log.e("TaskActionReceiver", "Error enqueueing work", e)
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

                        Log.d("TaskActionReceiver", "Snooze enqueued for task $taskId, $snoozeMinutes minutes")
                    } catch (e: Exception) {
                        Log.e("TaskActionReceiver", "Error enqueueing snooze work", e)
                    }
                }
            }
        }
    }
}