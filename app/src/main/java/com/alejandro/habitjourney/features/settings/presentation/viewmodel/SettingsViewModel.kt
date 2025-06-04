package com.alejandro.habitjourney.features.settings.presentation.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.features.settings.domain.repository.SettingsRepository
import com.alejandro.habitjourney.features.settings.presentation.screen.Language
import com.alejandro.habitjourney.features.settings.presentation.screen.ThemeMode
import com.alejandro.habitjourney.features.settings.presentation.state.SettingsUiState
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import com.alejandro.habitjourney.features.user.domain.usecase.DeleteUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
    private val deleteUserUseCase: DeleteUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
        loadSettings()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            userRepository.getLocalUser().collect { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getAppSettings().collect { settings ->
                _uiState.update {
                    it.copy(
                        currentTheme = mapToThemeMode(settings.theme),
                        currentLanguage = mapToLanguage(settings.language)
                    )
                }
            }
        }
    }

    fun updateTheme(themeMode: ThemeMode) {
        viewModelScope.launch {
            val themeString = when (themeMode) {
                ThemeMode.LIGHT -> "light"
                ThemeMode.DARK -> "dark"
                ThemeMode.SYSTEM -> "system"
            }
            settingsRepository.updateTheme(themeString)

            // Aplica el cambio a nivel de aplicación
            val appCompatThemeMode = when (themeMode) {
                ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(appCompatThemeMode)

            _uiState.update { it.copy(currentTheme = themeMode) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = userRepository.logout()) {
                is NetworkResponse.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            navigateToAuth = true
                        )
                    }
                }
                is NetworkResponse.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = "Error al cerrar sesión"
                        )
                    }
                }
                is NetworkResponse.Loading -> {
                    // No-op
                }
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = deleteUserUseCase()) {
                is NetworkResponse.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            navigateToAuth = true
                        )
                    }
                }
                is NetworkResponse.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = result.exception.message ?: "Error al eliminar la cuenta"
                        )
                    }
                }
                is NetworkResponse.Loading -> {
                    // No-op
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(navigateToAuth = false) }
    }

    private fun mapToThemeMode(theme: String): ThemeMode {
        return when (theme) {
            "light" -> ThemeMode.LIGHT
            "dark" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
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