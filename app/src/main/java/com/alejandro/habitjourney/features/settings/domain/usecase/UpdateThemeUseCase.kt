package com.alejandro.habitjourney.features.settings.domain.usecase

import com.alejandro.habitjourney.features.settings.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Caso de uso para actualizar el tema de la aplicación.
 *
 * Este caso de uso encapsula la lógica para cambiar y persistir
 * la preferencia del tema de la aplicación.
 *
 * @property settingsRepository El repositorio de configuraciones que maneja la persistencia.
 */
class UpdateThemeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Actualiza la preferencia del tema de la aplicación.
     *
     * @param theme El identificador del tema a guardar (ej: "light", "dark", "system").
     */
    suspend operator fun invoke(theme: String) {
        settingsRepository.updateTheme(theme)
    }
}