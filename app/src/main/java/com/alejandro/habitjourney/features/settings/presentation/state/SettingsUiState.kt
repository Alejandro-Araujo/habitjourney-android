package com.alejandro.habitjourney.features.settings.presentation.state


import com.alejandro.habitjourney.features.settings.presentation.screen.Language
import com.alejandro.habitjourney.features.settings.presentation.screen.ThemeMode
import com.alejandro.habitjourney.features.user.domain.model.User


data class SettingsUiState(
    val user: User? = null,
    val currentTheme: ThemeMode = ThemeMode.SYSTEM,
    val currentLanguage: Language = Language("es", "Español"),
    val isLoading: Boolean = false,
    val message: String? = null,
    val navigateToAuth: Boolean = false
)