package com.alejandro.habitjourney.features.task.data.local

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import com.alejandro.habitjourney.core.utils.logging.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Clase de utilidad que ayuda a verificar y solicitar permisos relacionados con alarmas y notificaciones.
 * Proporciona métodos para comprobar el estado de los permisos y para lanzar Intents que lleven al usuario
 * a la configuración del sistema para concederlos.
 *
 * @property context El contexto de la aplicación, inyectado por Hilt.
 */
@Singleton
class AlarmPermissionHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Verifica si la aplicación tiene permiso para programar alarmas exactas.
     * Este permiso es requerido en Android S (API 31) y superiores.
     *
     * @return `true` si la aplicación puede programar alarmas exactas, `false` en caso contrario.
     */
    @OptIn(UnstableApi::class)
    fun canScheduleExactAlarms(): Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        AppLogger.d("AlarmPermissionHelper", "🔍 canScheduleExactAlarms: $result (SDK: ${Build.VERSION.SDK_INT})")
        return result
    }

    /**
     * Verifica si la aplicación tiene el permiso de publicación de notificaciones.
     * Este permiso es requerido explícitamente a partir de Android Tiramisu (API 33).
     *
     * @return `true` si la aplicación tiene el permiso de notificación, `false` en caso contrario.
     */
    @OptIn(UnstableApi::class)
    fun hasNotificationPermission(): Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        AppLogger.d("AlarmPermissionHelper", "🔍 hasNotificationPermission: $result (SDK: ${Build.VERSION.SDK_INT})")
        return result
    }

    /**
     * Verifica si las notificaciones están habilitadas para la aplicación en la configuración del sistema.
     * Esto va más allá del permiso de notificación, comprobando si el usuario las ha desactivado globalmente.
     *
     * @return `true` si las notificaciones están habilitadas, `false` en caso contrario.
     */
    @OptIn(UnstableApi::class)
    fun areNotificationsEnabled(): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val result = notificationManager.areNotificationsEnabled()
        AppLogger.d("AlarmPermissionHelper", "🔍 areNotificationsEnabled: $result")
        return result
    }

    /**
     * Determina si la aplicación necesita solicitar al usuario permisos relacionados con alarmas o notificaciones.
     *
     * @return `true` si se necesita alguna solicitud de permiso, `false` en caso contrario.
     */
    @OptIn(UnstableApi::class)
    fun needsPermissionRequest(): Boolean {
        val needsAlarm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactAlarms()
        val needsNotification = !hasNotificationPermission() || !areNotificationsEnabled()
        val result = needsAlarm || needsNotification

        AppLogger.d("AlarmPermissionHelper", "🔍 needsPermissionRequest: $result (Alarm: $needsAlarm, Notification: $needsNotification)")
        return result
    }

    /**
     * Obtiene una lista de los tipos de permisos de alarma o notificación que faltan.
     *
     * @return Una [List] de [PermissionType]s que representan los permisos ausentes.
     */
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

        AppLogger.d("AlarmPermissionHelper", "🔍 getMissingPermissions: $missing")
        return missing
    }

    /**
     * Lanza un Intent para solicitar al usuario que conceda el permiso para programar alarmas exactas.
     * Esto abre la pantalla de configuración del sistema específica para este permiso.
     */
    @OptIn(UnstableApi::class)
    fun requestExactAlarmPermission() {
        AppLogger.d("AlarmPermissionHelper", "🔧 Solicitando permisos de alarma exacta")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent().apply {
                    action = android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                AppLogger.d("AlarmPermissionHelper", "✅ Intent de permisos de alarma enviado")
            } catch (e: Exception) {
                AppLogger.e("AlarmPermissionHelper", "❌ Error abriendo configuración de alarmas exactas", e)
                openAppSettings()
            }
        }
    }

    /**
     * Lanza un Intent para llevar al usuario a la configuración de notificaciones de la aplicación.
     *
     * @see [android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS]
     */
    @OptIn(UnstableApi::class)
    fun requestNotificationSettings() {
        AppLogger.d("AlarmPermissionHelper", "🔧 Abriendo configuración de notificaciones")

        try {
            val intent = Intent().apply {
                action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AppLogger.d("AlarmPermissionHelper", "✅ Configuración de notificaciones abierta")
        } catch (e: Exception) {
            AppLogger.e("AlarmPermissionHelper", "❌ Error abriendo configuración de notificaciones", e)
            openAppSettings()
        }
    }

    /**
     * Abre la pantalla de configuración de detalles de la aplicación, como fallback si las solicitudes específicas fallan.
     *
     * @see [android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS]
     */
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
            AppLogger.e("AlarmPermissionHelper", "❌ Error abriendo configuración de app", e)
        }
    }
}

/**
 * Enumeración que define los diferentes tipos de permisos relacionados con alarmas y notificaciones
 * que la aplicación puede necesitar.
 */
enum class PermissionType {
    EXACT_ALARM,
    NOTIFICATION_PERMISSION,
    NOTIFICATION_SETTINGS
}