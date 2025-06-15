package com.alejandro.habitjourney.features.settings.presentation.state

import com.alejandro.habitjourney.features.user.domain.model.User

/**
 * Representa el estado de la UI para la pantalla principal de Configuración.
 *
 * @property user El usuario actualmente logueado, o null.
 * @property currentTheme El [ThemeMode] actual de la aplicación.
 * @property currentLanguage El [Language] actual de la aplicación.
 * @property isLoading `true` si se están cargando los datos iniciales.
 * @property message Un mensaje informativo o de éxito para mostrar al usuario (ej: en un Snackbar).
 * @property navigateToAuth `true` si se debe navegar a la pantalla de autenticación (ej: tras cerrar sesión).
 */
data class SettingsUiState(
    val user: User? = null,
    val currentTheme: ThemeMode = ThemeMode.SYSTEM,
    val currentLanguage: Language = Language.fromCode("es"),
    val isLoading: Boolean = false,
    val message: String? = null,
    val navigateToAuth: Boolean = false
)

/**
 * Define los modos de tema disponibles en la aplicación.
 */
enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

/**
 * Representa un idioma seleccionable en la aplicación y centraliza la lógica de idiomas.
 *
 * @property code El código de idioma ISO (ej: "es", "en").
 * @property displayName El nombre del idioma para mostrar en la UI (ej: "Español", "English").
 * @property nativeName El nombre del idioma en su propia lengua (ej: "Idioma español").
 */
data class Language(
    val code: String,
    val displayName: String,
    val nativeName: String
) {
    companion object {
        /** La lista de todos los idiomas soportados, actuando como única fuente de verdad. */
        val allLanguages: List<Language> by lazy {
            listOf(
                Language("es", "Español", "Idioma español"),
                Language("en", "English", "English language"),
                Language("fr", "Français", "Langue française"),
                Language("de", "Deutsch", "Deutsche Sprache")
            )
        }

        /**
         * Obtiene un objeto [Language] a partir de su código ISO.
         * Si el código no es válido o es nulo, devuelve el idioma por defecto (Español).
         * @param code El código de idioma a buscar (ej: "es").
         * @return El [Language] correspondiente.
         */
        fun fromCode(code: String?): Language {
            return allLanguages.find { it.code == code } ?: allLanguages.first { it.code == "es" }
        }
    }
}


/**
 * Representa el estado de la UI para la pantalla de selección de idioma.
 *
 * @property currentLanguage El idioma actualmente seleccionado.
 * @property previousLanguage El idioma que estaba seleccionado antes de cualquier cambio.
 * @property languageChanged `true` si el idioma actual es diferente del anterior.
 */
data class LanguageUiState(
    val currentLanguage: Language = Language.fromCode("es"),
    val previousLanguage: Language = Language.fromCode("es"),
    val languageChanged: Boolean = false
)
