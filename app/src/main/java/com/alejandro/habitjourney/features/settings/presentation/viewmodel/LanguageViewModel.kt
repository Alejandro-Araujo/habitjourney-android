package com.alejandro.habitjourney.features.settings.presentation.viewmodel


import android.app.Activity
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat.recreate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.alejandro.habitjourney.features.settings.domain.repository.SettingsRepository
import com.alejandro.habitjourney.features.settings.presentation.screen.Language
import com.alejandro.habitjourney.features.settings.presentation.state.LanguageUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LanguageUiState())
    val uiState: StateFlow<LanguageUiState> = _uiState.asStateFlow()

    init {
        loadCurrentLanguage()
    }

    private fun loadCurrentLanguage() {
        viewModelScope.launch {
            settingsRepository.getAppSettings().collect { settings ->
                val language = mapToLanguage(settings.language)
                _uiState.update {
                    it.copy(
                        currentLanguage = language,
                        previousLanguage = if (it.previousLanguage.code == "es") language else it.previousLanguage
                    )
                }
            }
        }
    }

    fun updateLanguage(language: Language) {
        if (language.code == _uiState.value.currentLanguage.code) return

        viewModelScope.launch {
            // Guardar en el repository
            settingsRepository.updateLanguage(language.code)

            // Aplicar el cambio usando AppCompatDelegate
            val appLocale = LocaleListCompat.forLanguageTags(language.code)
            AppCompatDelegate.setApplicationLocales(appLocale)

            _uiState.update {
                it.copy(
                    currentLanguage = language,
                    languageChanged = true
                )
            }
        }
    }

    private fun mapToLanguage(languageCode: String): Language {
        return when (languageCode) {
            "en" -> Language("en", "English")
            "es" -> Language("es", "Español")
            "fr" -> Language("fr", "Français")
            "de" -> Language("de", "Deutsch")
            else -> Language("es", "Español")
        }
    }
}