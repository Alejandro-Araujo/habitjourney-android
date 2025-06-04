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

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
    }

    override fun getAppSettings(): Flow<AppSettings> {
        return context.settingsDataStore.data.map { preferences ->
            AppSettings(
                theme = preferences[THEME_KEY] ?: "system",
                language = preferences[LANGUAGE_KEY] ?: getCurrentLanguage()
            )
        }
    }

    override suspend fun updateTheme(theme: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    override suspend fun updateLanguage(language: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    private fun getCurrentLanguage(): String {
        return context.resources.configuration.locales[0].language
    }
}