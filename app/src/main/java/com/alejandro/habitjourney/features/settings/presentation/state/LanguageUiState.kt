package com.alejandro.habitjourney.features.settings.presentation.state


import com.alejandro.habitjourney.features.settings.presentation.screen.Language


data class LanguageUiState(
    val currentLanguage: Language = Language("es", "Español"),
    val previousLanguage: Language = Language("es", "Español"),
    val languageChanged: Boolean = false
)
