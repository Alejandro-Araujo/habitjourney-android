package com.alejandro.habitjourney.features.settings.domain.repository


import com.alejandro.habitjourney.features.settings.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getAppSettings(): Flow<AppSettings>
    suspend fun updateTheme(theme: String)
    suspend fun updateLanguage(language: String)
}