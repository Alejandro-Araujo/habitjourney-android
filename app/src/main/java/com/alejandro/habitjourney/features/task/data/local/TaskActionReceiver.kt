package com.alejandro.habitjourney.features.task.data.local


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.features.task.domain.usecase.ToggleTaskCompletionUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TaskActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase

    private val scope = CoroutineScope(SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("taskId", -1)

        when (intent.action) {
            "COMPLETE_TASK" -> {
                if (taskId != -1L) {
                    scope.launch {
                        try {
                            toggleTaskCompletionUseCase(taskId, true)
                            Toast.makeText(context,
                                context.getString(R.string.task_completed),
                                Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context,
                                context.getString(R.string.error_completing_task),
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}