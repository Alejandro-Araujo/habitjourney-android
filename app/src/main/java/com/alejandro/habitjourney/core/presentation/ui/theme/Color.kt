package com.alejandro.habitjourney.core.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color

// Colores Primarios (de tu guía de estilo)
val BaseOscura = Color(0xFF2D3748) // Azul oscuro para textos principales y fondos oscuros
val BaseClara = Color(0xFFF8FAFC) // Blanco suave para fondos claros

// Colores Funcionales
val AcentoPositivo = Color(0xFF48BB78) // Verde para acciones positivas/completadas
val AcentoUrgente = Color(0xFFED8936)  // Naranja para recordatorios y prioridad alta
val AcentoInformativo = Color(0xFF4299E1) // Azul para información y edición
val PrimaryVariant = Color(0xFF336699) //  Azul más oscuro que AcentoInformativo

// Colores de Gamificación
val Logro = Color(0xFFECC94B)     // Dorado para logros y recompensas
val Premium = Color(0xFF9F7AEA)   // Morado para elementos premium o avanzados

// Colores de Estado
val InactivoDeshabilitado = Color(0xFFA0AEC0) // Gris medio para elementos inactivos
val Error = Color(0xFFF56565)     // Rojo para mensajes de error
val Exito = Color(0xFF48BB78)     // Verde para mensajes de éxito (igual que AcentoPositivo, lo cual es coherente)

// Mapeo a la paleta de Material Design 3 (M3) para facilitar el uso
// Puedes ajustar cuál de tus colores funcionales se mapea a 'Primary' según la identidad de tu marca principal.
// He elegido AcentoInformativo porque es un azul común para botones y elementos principales.
val Primary = AcentoInformativo
val OnPrimary = Color.White // Texto sobre el color primario (blanco para contraste)

val Secondary = AcentoPositivo // Un color secundario, ideal para énfasis
val OnSecondary = Color.White // Texto sobre el color secundario

val Tertiary = AcentoUrgente // Otro color para acentuar o advertencias
val OnTertiary = Color.White // Texto sobre el color terciario

val Background = BaseClara // Fondo principal de la app (claro)
val OnBackground = BaseOscura // Color de texto principal sobre el fondo

val Surface = BaseClara // Superficies de componentes como Cards o Sheets
val OnSurface = BaseOscura // Color de texto sobre superficies

val ErrorColor = Error // Color de error para MaterialTheme
val OnError = Color.White // Texto sobre el color de error

// Colores específicos de tu guía que puedes usar directamente si no encajan en la paleta M3 estándar
val Gold = Logro
val Purple = Premium

val ListCompletedItem = AcentoPositivo.copy(alpha = 0.6f)
val NoteTypeIndicator = AcentoInformativo