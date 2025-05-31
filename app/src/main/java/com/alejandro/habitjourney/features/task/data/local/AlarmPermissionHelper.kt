package com.alejandro.habitjourney.features.task.data.local

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AlarmPermissionHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @OptIn(UnstableApi::class)
    fun canScheduleExactAlarms(): Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        Log.d("AlarmPermissionHelper", "🔍 canScheduleExactAlarms: $result (SDK: ${Build.VERSION.SDK_INT})")
        return result
    }

    @OptIn(UnstableApi::class)
    fun hasNotificationPermission(): Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android < 13 no necesita permiso explícito
            true
        }
        Log.d("AlarmPermissionHelper", "🔍 hasNotificationPermission: $result (SDK: ${Build.VERSION.SDK_INT})")
        return result
    }

    @OptIn(UnstableApi::class)
    fun areNotificationsEnabled(): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val result = notificationManager.areNotificationsEnabled()
        Log.d("AlarmPermissionHelper", "🔍 areNotificationsEnabled: $result")
        return result
    }

    @OptIn(UnstableApi::class)
    fun needsPermissionRequest(): Boolean {
        val needsAlarm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactAlarms()
        val needsNotification = !hasNotificationPermission() || !areNotificationsEnabled()
        val result = needsAlarm || needsNotification

        Log.d("AlarmPermissionHelper", "🔍 needsPermissionRequest: $result (Alarm: $needsAlarm, Notification: $needsNotification)")
        return result
    }

    @OptIn(UnstableApi::class)
    fun getMissingPermissions(): List<PermissionType> {
        val missing = mutableListOf<PermissionType>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactAlarms()) {
            missing.add(PermissionType.EXACT_ALARM)
        }

        if (!hasNotificationPermission()) {
            missing.add(PermissionType.NOTIFICATION_PERMISSION)
        }

        if (!areNotificationsEnabled()) {
            missing.add(PermissionType.NOTIFICATION_SETTINGS)
        }

        Log.d("AlarmPermissionHelper", "🔍 getMissingPermissions: $missing")
        return missing
    }

    @OptIn(UnstableApi::class)
    fun requestExactAlarmPermission() {
        Log.d("AlarmPermissionHelper", "🔧 Solicitando permisos de alarma exacta")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent().apply {
                    action = android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                Log.d("AlarmPermissionHelper", "✅ Intent de permisos de alarma enviado")
            } catch (e: Exception) {
                Log.e("AlarmPermissionHelper", "❌ Error abriendo configuración de alarmas exactas", e)
                openAppSettings()
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun requestNotificationSettings() {
        Log.d("AlarmPermissionHelper", "🔧 Abriendo configuración de notificaciones")

        try {
            val intent = Intent().apply {
                action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Log.d("AlarmPermissionHelper", "✅ Configuración de notificaciones abierta")
        } catch (e: Exception) {
            Log.e("AlarmPermissionHelper", "❌ Error abriendo configuración de notificaciones", e)
            openAppSettings()
        }
    }

    @OptIn(UnstableApi::class)
    private fun openAppSettings() {
        try {
            val intent = Intent().apply {
                action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = android.net.Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("AlarmPermissionHelper", "❌ Error abriendo configuración de app", e)
        }
    }
}

enum class PermissionType {
    EXACT_ALARM,
    NOTIFICATION_PERMISSION,
    NOTIFICATION_SETTINGS
}