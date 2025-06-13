package com.alejandro.habitjourney.core.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    background = BaseOscura, // Fondo oscuro para modo oscuro
    onBackground = BaseClara, // Texto claro sobre fondo oscuro
    surface = BaseOscura, // Superficies oscuras
    onSurface = BaseClara, // Texto claro sobre superficies oscuras
    error = ErrorColor,
    onError = OnError
)

// Define el esquema de color para el modo claro
private val LightColorScheme = lightColorScheme(
    primary = Primary, // El color principal de tu marca
    onPrimary = OnPrimary, // Color de texto sobre el primary
    secondary = Secondary, // Un color secundario
    onSecondary = OnSecondary,
    tertiary = Tertiary, // Un tercer color de acento
    onTertiary = OnTertiary,
    background = Background, // Color de fondo claro
    onBackground = OnBackground, // Color de texto sobre fondo claro
    surface = Surface, // Color de superficies claras
    onSurface = OnSurface, // Color de texto sobre superficies claras
    error = ErrorColor, // Color de error
    onError = OnError // Color de texto sobre error
)

@Composable
fun HabitJourneyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color está activado por defecto en Android 12+
    //dynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    dynamicColor: Boolean = false,

    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Dynamic color está disponible en Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Fallback a tu paleta personalizada
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}