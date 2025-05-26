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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions
import com.alejandro.habitjourney.core.presentation.ui.theme.HabitJourneyTheme

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

// --- PREVIEWS ---

@Preview(showBackground = true)
@Composable
fun PreviewHabitJourneyItem() {
    HabitJourneyTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
        ) {
            // Item Básico
            HabitJourneyItem(
                title = stringResource(R.string.item_task_title_1),
                subtitle = stringResource(R.string.item_task_subtitle_1),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Task,
                        contentDescription = stringResource(R.string.content_description_task_icon),
                        modifier = Modifier.size(Dimensions.IconSizeNormal),
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                onClick = { /* Handle click */ }
            )

            // Item con solo título y trailing icon
            HabitJourneyItem(
                title = stringResource(R.string.item_task_title_2),
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(R.string.content_description_go_to_details),
                        modifier = Modifier.size(Dimensions.IconSizeNormal),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = { /* Handle click */ }
            )

            // Item con solo título
            HabitJourneyItem(
                title = stringResource(R.string.item_task_title_3),
                onClick = { /* Handle click */ }
            )

            // Item deshabilitado
            HabitJourneyItem(
                title = stringResource(R.string.item_task_title_disabled),
                subtitle = stringResource(R.string.item_task_subtitle_disabled),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.content_description_completed_habit),
                        modifier = Modifier.size(Dimensions.IconSizeNormal),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                },
                isEnabled = false,
                onClick = { /* No debería ser clickeable */ }
            )

            // Item con borde (ejemplo)
            HabitJourneyItem(
                title = stringResource(R.string.item_task_title_urgent),
                subtitle = stringResource(R.string.item_task_subtitle_urgent),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = stringResource(R.string.content_description_warning_icon),
                        modifier = Modifier.size(Dimensions.IconSizeNormal),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                },
                borderColor = MaterialTheme.colorScheme.tertiary,
                borderWidth = Dimensions.BorderWidth,
                onClick = { /* Handle click */ }
            )
        }
    }
}