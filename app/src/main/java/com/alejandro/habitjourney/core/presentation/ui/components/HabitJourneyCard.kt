package com.alejandro.habitjourney.core.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions
import com.alejandro.habitjourney.core.presentation.ui.theme.HabitJourneyTheme // Para el Preview

@Composable
fun HabitJourneyCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null, // Hacerlo opcional, no todas las tarjetas serán clickeables
    containerColor: Color = MaterialTheme.colorScheme.surface, // Por defecto, color de superficie del tema
    content: @Composable ColumnScope.() -> Unit // Contenido de la tarjeta
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier), // Aplicar clickable solo si se proporciona onClick
        shape = RoundedCornerShape(Dimensions.CornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.ElevationLevel1)
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.SpacingMedium) // Padding interno por defecto
        ) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHabitJourneyCard() {
    HabitJourneyTheme {
        Column(modifier = Modifier.padding(Dimensions.SpacingMedium)) {
            HabitJourneyCard {
                Text(text = "Esto es una tarjeta genérica.", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Puede contener cualquier contenido.", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
            HabitJourneyCard(onClick = { /* clicked */ }) {
                Text(text = "Tarjeta Clickeable", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Haz clic para ver la acción.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}