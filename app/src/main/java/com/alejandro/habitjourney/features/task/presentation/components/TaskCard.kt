package com.alejandro.habitjourney.features.task.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyCard
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import com.alejandro.habitjourney.features.task.domain.model.Task
import kotlinx.datetime.*


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskCard(
    task: Task,
    onTaskClick: () -> Unit,
    onTaskLongClick: () -> Unit,
    onToggleCompletion: (Boolean) -> Unit,
    onArchiveTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showContextMenu by remember { mutableStateOf(false) }
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val isOverdue = task.dueDate?.let { it < now } == true && !task.isCompleted

    // Usar tu HabitJourneyCard general como base
    HabitJourneyCard(
        modifier = modifier.combinedClickable(
            onClick = onTaskClick,
            onLongClick = {
                showContextMenu = true
                onTaskLongClick()
            }
        ),
        containerColor = if (task.isCompleted) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleCompletion(it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = AcentoPositivo,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )

            Spacer(modifier = Modifier.width(Dimensions.SpacingMedium))

            // Contenido de la tarea
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Título
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                    ),
                    color = if (task.isCompleted) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Información adicional (fecha, prioridad)
                task.dueDate?.let { dueDate ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(Dimensions.IconSizeSmall),
                            tint = if (isOverdue) Error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = TaskDateUtils.formatDate(dueDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isOverdue) Error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Indicador de prioridad
            task.priority?.let { priority ->
                TaskPriorityIndicator(
                    priority = priority,
                    modifier = Modifier.padding(start = Dimensions.SpacingSmall)
                )
            }

            // Menú contextual
            Box {
                IconButton(
                    onClick = { showContextMenu = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.more_options),
                        modifier = Modifier.size(Dimensions.IconSizeSmall)
                    )
                }

                TaskContextMenu(
                    expanded = showContextMenu,
                    onDismiss = { showContextMenu = false },
                    task = task,
                    onArchiveTask = {
                        onArchiveTask()
                        showContextMenu = false
                    }
                )
            }
        }
    }
}
