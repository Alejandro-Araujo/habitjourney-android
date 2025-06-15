package com.alejandro.habitjourney.features.settings.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.alejandro.habitjourney.features.settings.domain.model.AppSettings
import com.alejandro.habitjourney.features.settings.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extensión de [Context] para proporcionar una instancia singleton de [DataStore] para las preferencias de la app.
 */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Implementación de [SettingsRepository] que utiliza Jetpack DataStore para persistir
 * las configuraciones del usuario de forma local y asíncrona.
 *
 * @property context El contexto de la aplicación, inyectado por Hilt, para acceder a DataStore.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    /**
     * Contiene las claves utilizadas para almacenar y recuperar datos de DataStore.
     */
    private companion object {
        val THEME_KEY = stringPreferencesKey("theme")
        val LANGUAGE_KEY = stringPreferencesKey("language")
    }

    /**
     * Obtiene las configuraciones de la aplicación como un [Flow].
     *
     * Emite un nuevo objeto [AppSettings] cada vez que una preferencia cambia.
     * Proporciona valores por defecto si no se ha guardado ninguna preferencia.
     *
     * @return Un [Flow] que emite los [AppSettings] actuales.
     */
    override fun getAppSettings(): Flow<AppSettings> {
        return context.settingsDataStore.data.map { preferences ->
            AppSettings(
                theme = preferences[THEME_KEY] ?: "system", // 'system' como valor por defecto
                language = preferences[LANGUAGE_KEY] ?: getCurrentLanguage()
            )
        }
    }

    /**
     * Actualiza y persiste la preferencia del tema de la aplicación.
     * @param theme El identificador del tema a guardar (ej: "light", "dark", "system").
     */
    override suspend fun updateTheme(theme: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    /**
     * Actualiza y persiste la preferencia del idioma de la aplicación.
     * @param language El código del idioma a guardar (ej: "en", "es").
     */
    override suspend fun updateLanguage(language: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    /**
     * Obtiene el idioma actual del dispositivo como valor por defecto.
     * @return El código de idioma principal del dispositivo (ej: "es").
     */
    private fun getCurrentLanguage(): String {
        return context.resources.configuration.locales[0].language
    }
}
