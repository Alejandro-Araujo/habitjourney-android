package com.alejandro.habitjourney.core.utils.resources

import androidx.annotation.StringRes

/**
 * Interfaz para acceso a recursos de strings sin contexto directo.
 * Útil en ViewModels y casos de uso que necesitan strings.
 */
interface ResourceProvider {
    fun getString(@StringRes resId: Int): String
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String
    fun getQuantityString(resId: Int, quantity: Int, vararg formatArgs: Any): String
}
