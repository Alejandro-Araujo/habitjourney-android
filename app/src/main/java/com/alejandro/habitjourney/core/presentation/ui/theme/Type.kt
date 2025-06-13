package com.alejandro.habitjourney.core.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import com.alejandro.habitjourney.R


val Roboto = FontFamily(
    Font(R.font.roboto_bold, FontWeight.Bold)
)

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium)
)

val FiraCode = FontFamily(
    Font(R.font.firacode_regular, FontWeight.Normal)
)

// Configura los estilos de texto de Material Design 3 con tus fuentes y tamaños
val Typography = Typography(
    // Títulos - Roboto Bold
    displayLarge = TextStyle( // Usado para H1: 24sp
        fontFamily = Roboto,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    headlineMedium = TextStyle( // Usado para H2: 20sp
        fontFamily = Roboto,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    headlineSmall = TextStyle( // Usado para H3: 18sp
        fontFamily = Roboto,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    ),
    // Texto Principal - Inter Medium/Regular
    bodyLarge = TextStyle( // Usado para Cuerpo: 16sp
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle( // Usado para Captions: 14sp
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    bodySmall = TextStyle( // Usado para Small: 12sp
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    // Datos Numéricos - Fira Code
    labelLarge = TextStyle( // Para estadísticas
        fontFamily = FiraCode,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelMedium = TextStyle( // Para contadores
        fontFamily = FiraCode,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    )
)