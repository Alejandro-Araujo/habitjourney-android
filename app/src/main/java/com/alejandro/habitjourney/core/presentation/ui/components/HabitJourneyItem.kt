package com.alejandro.habitjourney.core.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions

/**
 * Componente base para items de lista con layout leading-content-trailing.
 * Clickeable opcional y soporte para estados disabled.
 *
 * @param title Texto principal del item
 * @param subtitle Texto secundario opcional
 * @param leadingContent Composable al inicio (ej: checkbox, avatar)
 * @param trailingContent Composable al final (ej: flecha, botón)
 * @param onClick Callback para hacer el item clickeable
 * @param isEnabled Si el item está habilitado visualmente
 */
@Composable
fun HabitJourneyItem(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 0.dp,
    isEnabled: Boolean = true,
    elevation: Dp = Dimensions.ElevationLevel1

) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimensions.TaskItemHeight)
            .then(if (onClick != null && isEnabled) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(Dimensions.CornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.ElevationLevel1),
        border = if (borderColor != Color.Transparent && borderWidth > 0.dp) {
            BorderStroke(borderWidth, borderColor)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Dimensions.SpacingMedium,
                    vertical = Dimensions.SpacingSmall
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                leadingContent?.invoke()

                if (leadingContent != null) {
                    Spacer(modifier = Modifier.width(Dimensions.SpacingMedium))
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isEnabled) 1f else 0.5f),
                        maxLines = 1
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isEnabled) 1f else 0.5f),
                            maxLines = 1
                        )
                    }
                }
            }

            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
                trailingContent.invoke()
            }
        }
    }
}
