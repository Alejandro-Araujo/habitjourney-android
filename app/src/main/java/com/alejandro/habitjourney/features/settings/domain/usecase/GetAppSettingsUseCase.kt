package com.alejandro.habitjourney.features.settings.domain.usecase

import com.alejandro.habitjourney.features.settings.domain.model.AppSettings
import com.alejandro.habitjourney.features.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener las configuraciones de la aplicación.
 *
 * Este caso de uso proporciona acceso a las configuraciones almacenadas
 * como tema e idioma de la aplicación, emitiendo los datos como un [Flow]
 * para permitir una observación reactiva.
 *
 * @property settingsRepository El repositorio de configuraciones que proporciona acceso a los datos.
 */
class GetAppSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Obtiene las configuraciones de la aplicación.
     *
     * @return Un [Flow] que emite el objeto [AppSettings] actual.
     */
    operator fun invoke(): Flow<AppSettings> {
        return settingsRepository.getAppSettings()
    }
}