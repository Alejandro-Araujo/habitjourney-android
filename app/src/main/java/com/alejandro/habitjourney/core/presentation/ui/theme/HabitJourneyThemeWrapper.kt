package com.alejandro.habitjourney.core.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.alejandro.habitjourney.features.settings.data.repository.SettingsRepositoryImpl

/**
 * Wrapper de tema que observa configuraciones del usuario y aplica tema dinámicamente.
 * Conecta el sistema de settings con el tema de Compose.
 *
 * Funcionalidades:
 * - Observa cambios en settings de tema
 * - Soporta Light/Dark/Sistema
 * - Fallback a tema del sistema
 */
@Composable
fun HabitJourneyThemeWrapper(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepositoryImpl(context) }

    val appSettings by settingsRepository.getAppSettings().collectAsState(
        initial = com.alejandro.habitjourney.features.settings.domain.model.AppSettings()
    )

    val useDarkTheme = when (appSettings.theme) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    HabitJourneyTheme(
        darkTheme = useDarkTheme,
        content = content
    )
}