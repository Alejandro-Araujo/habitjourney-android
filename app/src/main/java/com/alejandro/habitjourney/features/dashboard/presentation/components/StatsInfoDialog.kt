package com.alejandro.habitjourney.features.dashboard.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyDialog
import com.alejandro.habitjourney.core.presentation.ui.theme.*

/**
 * Dialog informativo que explica cómo se calculan las estadísticas del dashboard.
 * Incluye información sobre rachas y puntuación de productividad.
 *
 * @param onDismiss Callback cuando se cierra el dialog
 */
@Composable
fun CalculationInfoDialog(onDismiss: () -> Unit) {
    HabitJourneyDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.stats_info_title),
        message = stringResource(R.string.stats_info_description),
        icon = Icons.Default.Info,
        confirmButtonText = stringResource(R.string.action_understood),
        onConfirm = onDismiss
    ) {
        // Contenido personalizado para el diálogo
        Column(verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)) {
            // Sección Racha
            InfoSection(
                icon = Icons.Default.LocalFireDepartment,
                title = stringResource(R.string.stats_info_streak_title),
                color = Logro
            ) {
                Text(
                    text = stringResource(R.string.stats_info_streak_desc_simple), // Nuevo string
                    style = Typography.bodyMedium
                )
            }

            HorizontalDivider()

            // Sección Puntuación de Productividad
            InfoSection(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                title = stringResource(R.string.stats_info_score_title),
                color = AcentoPositivo
            ) {
                Text(
                    text = stringResource(R.string.stats_info_score_desc_main), // Nuevo string
                    style = Typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
                Text(
                    text = stringResource(R.string.stats_info_score_desc_breakdown), // Nuevo string
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Sección informativa reutilizable con icono, título y contenido personalizado.
 */
@Composable
private fun InfoSection(
    icon: ImageVector,
    title: String,
    color: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(Dimensions.IconSizeNormal)
            )
            Text(
                text = title,
                style = Typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
        Column(
            modifier = Modifier.padding(start = Dimensions.SpacingSmall),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingExtraSmall)
        ) {
            content()
        }
    }
}