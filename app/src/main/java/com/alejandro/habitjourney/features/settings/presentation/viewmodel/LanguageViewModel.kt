package com.alejandro.habitjourney.features.settings.presentation.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.features.settings.domain.repository.SettingsRepository
import com.alejandro.habitjourney.features.settings.presentation.state.Language
import com.alejandro.habitjourney.features.settings.presentation.state.LanguageUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para gestionar la selección de idioma de la aplicación.
 *
 * Responsabilidades:
 * - Cargar el idioma actual de la aplicación
 * - Aplicar cambios de idioma tanto en el repositorio como en el sistema
 * - Gestionar el estado de cambio de idioma
 * - Proporcionar lista de idiomas disponibles
 */
@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LanguageUiState())
    val uiState: StateFlow<LanguageUiState> = _uiState.asStateFlow()

    init {
        loadCurrentLanguage()
    }

    /**
     * Carga el idioma actual desde las configuraciones guardadas.
     */
    private fun loadCurrentLanguage() {
        viewModelScope.launch {
            settingsRepository.getAppSettings().collect { settings ->
                val language = Language.fromCode(settings.language)
                _uiState.update {
                    it.copy(
                        currentLanguage = language,
                        // Solo actualizar idioma anterior si no está inicializado
                        previousLanguage = if (it.previousLanguage.code == "es") language else it.previousLanguage
                    )
                }
            }
        }
    }

    /**
     * Actualiza el idioma de la aplicación.
     * Aplica el cambio tanto en el repositorio como usando AppCompatDelegate.
     *
     * @param language Nuevo idioma a aplicar
     */
    fun updateLanguage(language: Language) {
        // No hacer nada si es el mismo idioma
        if (language.code == _uiState.value.currentLanguage.code) return

        viewModelScope.launch {
            // Guardar en el repository para persistencia
            settingsRepository.updateLanguage(language.code)

            // Aplicar el cambio usando AppCompatDelegate para efecto inmediato
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
}