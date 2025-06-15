package com.alejandro.habitjourney.features.settings.domain.repository

import com.alejandro.habitjourney.features.settings.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define el contrato para la gestión de las configuraciones de la aplicación.
 *
 * Actúa como una abstracción sobre la capa de datos, permitiendo que el dominio
 * interactúe con las preferencias del usuario sin conocer los detalles de su implementación.
 */
interface SettingsRepository {
    /**
     * Obtiene las configuraciones actuales de la aplicación de forma reactiva.
     * @return Un [Flow] que emite el objeto [AppSettings] cada vez que hay un cambio.
     */
    fun getAppSettings(): Flow<AppSettings>

    /**
     * Actualiza la preferencia del tema de la aplicación.
     * @param theme El identificador del tema a guardar (ej: "light", "dark", "system").
     */
    suspend fun updateTheme(theme: String)

    /**
     * Actualiza la preferencia del idioma de la aplicación.
     * @param language El código del idioma a guardar (ej: "es", "en").
     */
    suspend fun updateLanguage(language: String)
}
