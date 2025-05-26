package com.alejandro.habitjourney.core.presentation.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.core.presentation.ui.theme.HabitJourneyTheme

@Composable
fun HabitJourneyLoadingOverlay(
    modifier: Modifier = Modifier,
    // Puedes ajustar el color de fondo si lo deseas
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
    // Puedes ajustar el color del indicador
    indicatorColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(56.dp), // Tamaño estándar para un indicador grande
            color = indicatorColor,
            strokeWidth = 4.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoadingOverlay() {
    HabitJourneyTheme {
        HabitJourneyLoadingOverlay()
    }
}