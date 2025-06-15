package com.alejandro.habitjourney.core.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.core.presentation.ui.theme.*

/**
 * Estado vacío reutilizable con icono, texto y botón de acción opcional.
 * Centrado y con espaciado consistente.
 */
@Composable
fun HabitJourneyEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Inbox,
    iconTint: Color = InactivoDeshabilitado,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimensions.SpacingLarge),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = iconTint
            )

            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = BaseOscura,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = InactivoDeshabilitado,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            if (actionButtonText != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

                HabitJourneyButton(
                    text = actionButtonText,
                    onClick = onActionClick,
                    type = HabitJourneyButtonType.PRIMARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}