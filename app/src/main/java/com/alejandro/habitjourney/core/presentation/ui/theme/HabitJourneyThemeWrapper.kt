package com.alejandro.habitjourney.core.presentation.ui.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.alejandro.habitjourney.features.settings.data.repository.SettingsRepositoryImpl
import com.alejandro.habitjourney.features.settings.presentation.screen.ThemeMode
import com.alejandro.habitjourney.features.settings.presentation.viewmodel.SettingsViewModel

@Composable
fun HabitJourneyThemeWrapper(

    settingsViewModel: SettingsViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {

    val uiState by settingsViewModel.uiState.collectAsState()

    val useDarkTheme = when (uiState.currentTheme) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    HabitJourneyTheme(
        darkTheme = useDarkTheme,
        content = content
    )
}