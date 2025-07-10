package com.alejandro.habitjourney.features.settings.domain.usecase

import com.alejandro.habitjourney.features.settings.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Caso de uso para actualizar el idioma de la aplicación.
 *
 * Este caso de uso encapsula la lógica para cambiar y persistir
 * la preferencia del idioma de la aplicación.
 *
 * @property settingsRepository El repositorio de configuraciones que maneja la persistencia.
 */
class UpdateLanguageUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Actualiza la preferencia del idioma de la aplicación.
     *
     * @param language El código del idioma a guardar (ej: "en", "es").
     */
    suspend operator fun invoke(language: String) {
        settingsRepository.updateLanguage(language)
    }
}