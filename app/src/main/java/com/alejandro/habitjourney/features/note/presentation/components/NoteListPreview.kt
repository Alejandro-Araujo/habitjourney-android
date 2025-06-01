package com.alejandro.habitjourney.features.note.presentation.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyProgressIndicator
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyProgressType
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoPositivo
import com.alejandro.habitjourney.core.presentation.ui.theme.BaseOscura
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions
import com.alejandro.habitjourney.core.presentation.ui.theme.OnSurface
import com.alejandro.habitjourney.core.presentation.ui.theme.Typography
import com.alejandro.habitjourney.features.note.domain.model.Note

@Composable
fun NoteListPreview(
    note: Note,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Progress bar para listas
        val completedCount = note.listItems.count { it.isCompleted }
        val totalCount = note.listItems.size

        if (totalCount > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
            ) {
                HabitJourneyProgressIndicator(
                    progress = note.completionPercentage,
                    modifier = Modifier.weight(1f),
                    type = HabitJourneyProgressType.LINEAR,
                    progressColor = AcentoPositivo,
                    trackColor = OnSurface.copy(alpha = 0.12f),
                    animationDuration = 1000,
                    size = Dimensions.ProgressBarHeight
                )
                Text(
                    text = stringResource(R.string.list_completion, completedCount, totalCount),
                    style = Typography.bodySmall,
                    color = BaseOscura
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
        }

        // Preview de primeros items
        note.listItems.take(Dimensions.ListPreviewMaxItems).forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Icon(
                    imageVector = if (item.isCompleted) {
                        Icons.Default.CheckBox
                    } else {
                        Icons.Default.CheckBoxOutlineBlank
                    },
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.IconSizeSmall),
                    tint = if (item.isCompleted) {
                        AcentoPositivo
                    } else {
                        OnSurface
                    }
                )
                Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
                Text(
                    text = item.text.ifBlank { stringResource(R.string.list_item_hint) },
                    style = Typography.bodySmall.copy(
                        textDecoration = if (item.isCompleted) {
                            TextDecoration.LineThrough
                        } else {
                            null
                        }
                    ),
                    color = if (item.isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        OnSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontStyle = if (item.text.isBlank()) FontStyle.Italic else FontStyle.Normal
                )
            }
        }

        // Indicador de más items
        if (note.listItems.size > Dimensions.ListPreviewMaxItems) {
            Text(
                text = stringResource(
                    R.string.and_more_items,
                    note.listItems.size - Dimensions.ListPreviewMaxItems
                ),
                style = Typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic
            )
        }
    }
}