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
import com.alejandro.habitjourney.core.presentation.ui.components.ConfirmationDialog
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyCard
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyCardType
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import com.alejandro.habitjourney.features.task.domain.model.Task
import kotlinx.datetime.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskCard(
    modifier: Modifier = Modifier,
    task: Task,
    onTaskClick: () -> Unit,
    onTaskLongClick: () -> Unit,
    onToggleCompletion: (Boolean) -> Unit,
    onArchiveTask: () -> Unit,
    onUnarchiveTask: () -> Unit,
    onDeleteTask: (() -> Unit)? = null
) {
    var showMenuDropdown by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val isOverdue = task.dueDate?.let { it < now } == true && !task.isCompleted

    HabitJourneyCard(
        modifier = modifier.combinedClickable(
            onClick = onTaskClick,
            onLongClick = {
                showMenuDropdown = true
                onTaskLongClick()
            }
        ),
        containerColor = if (task.isArchived) {
            InactivoDeshabilitado.copy(alpha = AlphaValues.DisabledAlpha)
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor =  MaterialTheme.colorScheme.onSurface,
        cardType = HabitJourneyCardType.ELEVATED,
        elevation = Dimensions.ElevationLevel2
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
                    uncheckedColor = InactivoDeshabilitado
                ),
                enabled = !task.isArchived
            )

            Spacer(modifier = Modifier.width(Dimensions.SpacingMedium))

            // Contenido de la tarea
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Título
                Text(
                    text = task.title,
                    style = Typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                    ),
                    color = if (task.isCompleted) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaValues.MediumAlpha)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Descripción (si existe)
                task.description?.let { description ->
                    Text(
                        text = description,
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaValues.HighAlpha),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Información adicional (fecha, prioridad)
                Row(
                    modifier = Modifier.padding(top = Dimensions.SpacingExtraSmall),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
                ) {
                    // Fecha de vencimiento
                    task.dueDate?.let { dueDate ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(Dimensions.IconSizeSmall),
                                tint = if (isOverdue) Error else InactivoDeshabilitado
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = TaskDateUtils.formatDate(dueDate),
                                style = Typography.bodySmall,
                                color = if (isOverdue) Error else InactivoDeshabilitado
                            )
                        }
                    }

                    // Indicador de prioridad
                    task.priority?.let { priority ->
                        TaskPriorityIndicator(
                            priority = priority,
                        )
                    }
                }
            }

            // Menú contextual
            Box {
                IconButton(
                    onClick = { showMenuDropdown = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.more_options),
                        modifier = Modifier.size(Dimensions.IconSizeSmall)
                    )
                }

                TaskContextMenu(
                    expanded = showMenuDropdown,
                    onDismiss = { showMenuDropdown = false },
                    task = task,
                    onArchiveTask = {
                        showArchiveDialog = true
                        showMenuDropdown = false
                    },
                    onUnarchiveTask = {
                        showArchiveDialog = true
                        showMenuDropdown = false
                    },
                    onDeleteTask = if (onDeleteTask != null) {
                        {
                            showDeleteDialog = true
                            showMenuDropdown = false
                        }
                    } else null
                )

                // Diálogos al final del Composable
                if (showArchiveDialog) {
                    ConfirmationDialog(
                        onDismissRequest = { showArchiveDialog = false },
                        title = if (!task.isArchived) {
                            stringResource(R.string.archive_task_title)
                        } else {
                            stringResource(R.string.unarchive_task_title)
                        },
                        message = if (!task.isArchived) {
                            stringResource(R.string.archive_task_message)
                        } else {
                            stringResource(R.string.unarchive_task_message)
                        },
                        onConfirm = {
                            if (task.isArchived) onUnarchiveTask() else onArchiveTask()
                            showArchiveDialog = false
                        },
                        confirmText = if (!task.isArchived) {
                            stringResource(R.string.archive)
                        } else {
                            stringResource(R.string.unarchive)
                        },
                        cancelText = stringResource(R.string.cancel),
                        icon = Icons.Default.Archive
                    )
                }

                if (showDeleteDialog && onDeleteTask != null) {
                    ConfirmationDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = stringResource(R.string.delete_task_title),
                        message = stringResource(R.string.delete_task_message),
                        onConfirm = {
                            onDeleteTask()
                            showDeleteDialog = false
                        },
                        confirmText = stringResource(R.string.delete),
                        cancelText = stringResource(R.string.cancel),
                        icon = Icons.Default.Warning
                    )
                }
            }
        }
    }
}