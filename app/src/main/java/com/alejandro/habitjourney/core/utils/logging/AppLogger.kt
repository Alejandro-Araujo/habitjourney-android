package com.alejandro.habitjourney.core.utils.logging

import android.util.Log
import com.alejandro.habitjourney.BuildConfig

/**
 * Wrapper para logging que automáticamente desactiva logs en producción.
 *
 * Uso:
 * - AppLogger.d("TAG", "mensaje") en lugar de Log.d
 * - Se desactiva automáticamente en release builds
 * - Mantiene consistencia en el proyecto
 */
object AppLogger {

    private const val DEFAULT_TAG = "HabitJourney"

    /**
     * Log de debug - solo visible en debug builds
     */
    fun d(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.d(tag, message, throwable)
            } else {
                Log.d(tag, message)
            }
        }
    }

    /**
     * Log de información - solo visible en debug builds
     */
    fun i(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.i(tag, message, throwable)
            } else {
                Log.i(tag, message)
            }
        }
    }

    /**
     * Log de warning - visible en debug y release para monitoreo
     */
    fun w(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.w(tag, message, throwable)
        } else {
            Log.w(tag, message)
        }
    }

    /**
     * Log de error - siempre visible para crash reporting
     */
    fun e(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }

    /**
     * Log específico para ViewModels
     */
    fun vm(viewModelName: String, message: String) {
        d("VM_$viewModelName", message)
    }

    /**
     * Log específico para operaciones de red
     */
    fun network(message: String, throwable: Throwable? = null) {
        d("NETWORK", message, throwable)
    }

    /**
     * Log específico para base de datos
     */
    fun db(message: String, throwable: Throwable? = null) {
        d("DATABASE", message, throwable)
    }
}