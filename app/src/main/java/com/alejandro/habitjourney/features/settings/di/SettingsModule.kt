package com.alejandro.habitjourney.features.settings.di

import com.alejandro.habitjourney.features.settings.data.repository.SettingsRepositoryImpl
import com.alejandro.habitjourney.features.settings.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Dagger Hilt para la inyección de dependencias del feature de Configuración (Settings).
 *
 * Se encarga de vincular la implementación del repositorio [SettingsRepositoryImpl] a su interfaz
 * [SettingsRepository], permitiendo que otras clases dependan de la interfaz sin
 * conocer la implementación concreta.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {

    /**
     * Vincula la implementación [SettingsRepositoryImpl] a la interfaz [SettingsRepository].
     *
     * Se usa @Binds en lugar de @Provides para una mayor eficiencia en la generación de código
     * por parte de Hilt, ya que no necesita crear una instancia de fábrica para la vinculación.
     *
     * @param settingsRepositoryImpl La implementación concreta del repositorio.
     * @return Una instancia que satisface la dependencia de [SettingsRepository].
     */
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository
}
