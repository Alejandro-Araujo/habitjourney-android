package com.alejandro.habitjourney.features.settings.domain.model

/**
 * Representa el modelo de dominio para las configuraciones de la aplicación.
 *
 * Esta clase de datos contiene las preferencias del usuario que se pueden
 * modificar en la pantalla de configuración.
 *
 * @property theme El tema actual de la aplicación (ej: "light", "dark", "system").
 * @property language El código de idioma actual de la aplicación (ej: "es", "en").
 */
data class AppSettings(
    val theme: String = "system",
    val language: String = "es"
)