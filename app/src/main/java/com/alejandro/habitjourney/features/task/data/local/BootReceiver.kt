package com.alejandro.habitjourney.features.task.data.local

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alejandro.habitjourney.core.utils.logging.AppLogger
import dagger.hilt.android.AndroidEntryPoint

/**
 * [BroadcastReceiver] que escucha eventos de arranque del dispositivo y de actualización de la aplicación.
 *
 * Cuando el dispositivo se reinicia (`Intent.ACTION_BOOT_COMPLETED`) o la aplicación es actualizada
 * (`Intent.ACTION_MY_PACKAGE_REPLACED`), este receptor puede ser utilizado para reprogramar alarmas
 * o realizar otras inicializaciones que requieran que la aplicación se ejecute en segundo plano.
 * Requiere el permiso `RECEIVE_BOOT_COMPLETED` en el manifiesto de Android.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    /**
     * Se invoca cuando el sistema envía una transmisión.
     * Este receptor específicamente maneja las acciones de arranque completo del dispositivo
     * y la sustitución del paquete de la aplicación.
     *
     * @param context El [Context] en el que se ejecuta el receptor.
     * @param intent El [Intent] que se ha transmitido.
     */
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                AppLogger.d("BootReceiver", "Dispositivo reiniciado o app actualizada")
            }
        }
    }
}