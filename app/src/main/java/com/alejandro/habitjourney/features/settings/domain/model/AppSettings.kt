package com.alejandro.habitjourney.features.settings.domain.model


data class AppSettings(
    val theme: String = "system", // light, dark, system
    val language: String = "es" // es, en, fr, de
)