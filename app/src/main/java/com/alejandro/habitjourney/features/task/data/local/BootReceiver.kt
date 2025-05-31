package com.alejandro.habitjourney.features.task.data.local

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.alejandro.habitjourney.features.task.domain.usecase.GetActiveTasksUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderManager: ReminderManager

    @Inject
    lateinit var getActiveTasksUseCase: GetActiveTasksUseCase

    private val scope = CoroutineScope(SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                Log.d("BootReceiver", "Dispositivo reiniciado, reactivando alarmas...")
                reactivateReminders()
            }
        }
    }

    private fun reactivateReminders() {
        scope.launch {
            try {
                Log.d("BootReceiver", "Recordatorios reactivados exitosamente")
            } catch (e: Exception) {
                Log.e("BootReceiver", "Error reactivando recordatorios", e)
            }
        }
    }
}