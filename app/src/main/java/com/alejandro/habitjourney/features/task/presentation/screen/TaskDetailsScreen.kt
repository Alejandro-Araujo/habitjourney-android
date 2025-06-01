package com.alejandro.habitjourney.features.task.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.Priority
import com.alejandro.habitjourney.core.presentation.ui.components.*
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.task.presentation.components.*
import com.alejandro.habitjourney.features.task.presentation.viewmodel.TaskDetailsViewModel
import kotlinx.datetime.*

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(
    taskId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: TaskDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val task by viewModel.task.collectAsStateWithLifecycle()

    // Inicializar el ViewModel
    LaunchedEffect(taskId) {
        viewModel.initializeWithTaskId(taskId)
    }

    // Estados para diálogos de confirmación
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }
    var showMenuDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoading, uiState.taskExists) {
        if (!uiState.isLoading && !uiState.taskExists && uiState.error != null) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.task_details),
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                },
                actions = {
                    if (task != null && !uiState.isLoading) {
                        // Botón de editar
                        IconButton(
                            onClick = { onNavigateToEdit(taskId) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit_task)
                            )
                        }

                        // Menú con más opciones
                        Box {
                            IconButton(
                                onClick = { showMenuDropdown = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = stringResource(R.string.more_options)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenuDropdown,
                                onDismissRequest = { showMenuDropdown = false }
                            ) {
                                task?.let { currentTask ->
                                    if (!currentTask.isArchived) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.archive_task)) },
                                            onClick = {
                                                showArchiveDialog = true
                                                showMenuDropdown = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Archive,
                                                    contentDescription = null
                                                )
                                            }
                                        )
                                    } else {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.unarchive_task)) },
                                            onClick = {
                                                viewModel.archiveTask(onNavigateBack)
                                                showMenuDropdown = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Unarchive,
                                                    contentDescription = null
                                                )
                                            }
                                        )
                                    }

                                    HorizontalDivider()

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = stringResource(R.string.delete_task),
                                                color = Error
                                            )
                                        },
                                        onClick = {
                                            showDeleteDialog = true
                                            showMenuDropdown = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = Error
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            task?.let { currentTask ->
                TaskDetailsContent(
                    task = currentTask,
                    onToggleCompletion = viewModel::toggleTaskCompletion,
                    isProcessing = uiState.isProcessing,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            // Overlay de carga
            if (uiState.isLoading) {
                HabitJourneyLoadingOverlay()
            }
        }
    }

    // Diálogos de confirmación
    if (showDeleteDialog) {
        ConfirmationDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = stringResource(R.string.delete_task_title),
            message = stringResource(R.string.delete_task_message),
            onConfirm = {
                viewModel.deleteTask(onNavigateBack)
                showDeleteDialog = false
            },
            confirmText = stringResource(R.string.delete),
            cancelText = stringResource(R.string.cancel),
            icon = Icons.Default.Warning
        )
    }

    if (showArchiveDialog) {
        task?.let { currentTask ->
            ConfirmationDialog(
                onDismissRequest = { showArchiveDialog = false },
                title = if (!currentTask.isArchived) {
                    stringResource(R.string.archive_task_title)
                } else {
                    stringResource(R.string.unarchive_task_title)
                },
                message = if (!currentTask.isArchived) {
                    stringResource(R.string.archive_task_message)
                } else {
                    stringResource(R.string.unarchive_task_message)
                },
                onConfirm = {
                    viewModel.archiveTask(onNavigateBack)
                    showArchiveDialog = false
                },
                confirmText = if (!currentTask.isArchived) {
                    stringResource(R.string.archive)
                } else {
                    stringResource(R.string.unarchive)
                },
                cancelText = stringResource(R.string.cancel),
                icon = Icons.Default.Archive
            )
        }
    }
}

@Composable
private fun TaskDetailsContent(
    task: Task,
    onToggleCompletion: () -> Unit,
    isProcessing: Boolean,
    modifier: Modifier = Modifier
) {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val isOverdue = task.dueDate?.let { it < now } == true && !task.isCompleted

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(Dimensions.SpacingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingLarge)
    ) {
        // Sección principal
        TaskHeaderSection(
            task = task,
            onToggleCompletion = onToggleCompletion,
            isProcessing = isProcessing,
            isOverdue = isOverdue
        )

        // Descripción si existe
        if (!task.description.isNullOrBlank()) {
            TaskDescriptionSection(description = task.description)
        }

        // Información de fechas
        TaskDatesSection(
            task = task,
            isOverdue = isOverdue
        )

        // Prioridad si existe
        task.priority?.let { priority ->
            TaskPrioritySection(priority = priority)
        }

        // Recordatorio si existe
        if (task.isReminderSet && task.reminderDateTime != null) {
            TaskReminderSection(reminderDateTime = task.reminderDateTime)
        }

        // Estado de la tarea
        TaskStatusSection(task = task)
    }
}

@Composable
private fun TaskHeaderSection(
    task: Task,
    onToggleCompletion: () -> Unit,
    isProcessing: Boolean,
    isOverdue: Boolean,
    modifier: Modifier = Modifier
) {
    HabitJourneyCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Checkbox con indicador de carga
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = AcentoInformativo
                    )
                } else {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { onToggleCompletion() },
                        modifier = Modifier.size(24.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = AcentoPositivo,
                            uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(Dimensions.SpacingMedium))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        lineHeight = 28.sp
                    ),
                    color = if (task.isCompleted) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (isOverdue) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(Dimensions.IconSizeSmall),
                            tint = Error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.task_overdue),
                            style = MaterialTheme.typography.bodySmall.copy(
                                lineHeight = 16.sp
                            ),
                            color = Error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskDescriptionSection(
    description: String,
    modifier: Modifier = Modifier
) {
    HabitJourneyCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.IconSizeNormal),
                tint = AcentoInformativo
            )
            Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
            Text(
                text = stringResource(R.string.description),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TaskDatesSection(
    task: Task,
    isOverdue: Boolean,
    modifier: Modifier = Modifier
) {
    HabitJourneyCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.IconSizeNormal),
                tint = AcentoInformativo
            )
            Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
            Text(
                text = stringResource(R.string.dates),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

        // Fecha de vencimiento
        task.dueDate?.let { dueDate ->
            DateInfoRow(
                label = stringResource(R.string.due_date),
                date = TaskDateUtils.formatDateForDisplay(dueDate),
                isError = isOverdue
            )
        }

        // Fecha de finalización
        task.completionDate?.let { completionDate ->
            DateInfoRow(
                label = stringResource(R.string.completion_date),
                date = TaskDateUtils.formatDateForDisplay(completionDate),
                isError = false
            )
        }

        // Fecha de creación
        val createdDate = Instant.fromEpochMilliseconds(task.createdAt)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        DateInfoRow(
            label = stringResource(R.string.created_date),
            date = TaskDateUtils.formatDateForDisplay(createdDate),
            isError = false
        )
    }
}

@Composable
private fun DateInfoRow(
    label: String,
    date: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = date,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = if (isError) Error else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TaskPrioritySection(
    priority: Priority,
    modifier: Modifier = Modifier
) {
    HabitJourneyCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TaskPriorityIcon(priority = priority)
            Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
            Text(
                text = stringResource(R.string.priority),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TaskPriorityIndicator(priority = priority)
                Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
                Text(
                    text = when (priority) {
                        Priority.HIGH -> stringResource(R.string.priority_high)
                        Priority.MEDIUM -> stringResource(R.string.priority_medium)
                        Priority.LOW -> stringResource(R.string.priority_low)
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
private fun TaskReminderSection(
    reminderDateTime: LocalDateTime,
    modifier: Modifier = Modifier
) {
    HabitJourneyCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.IconSizeNormal),
                tint = AcentoInformativo
            )
            Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
            Text(
                text = stringResource(R.string.reminder),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = TaskDateUtils.formatDateTimeForDisplay(reminderDateTime),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun TaskStatusSection(
    task: Task,
    modifier: Modifier = Modifier
) {
    HabitJourneyCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.IconSizeNormal),
                tint = AcentoInformativo
            )
            Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
            Text(
                text = stringResource(R.string.task_status),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

        StatusInfoRow(
            label = stringResource(R.string.status),
            value = when {
                task.isArchived -> stringResource(R.string.archived)
                task.isCompleted -> stringResource(R.string.completed)
                else -> stringResource(R.string.active)
            },
            color = when {
                task.isArchived -> MaterialTheme.colorScheme.onSurfaceVariant
                task.isCompleted -> AcentoPositivo
                else -> AcentoInformativo
            }
        )
    }
}

@Composable
private fun StatusInfoRow(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = color
        )
    }
}

// Diálogo de confirmación
@Composable
private fun ConfirmationDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    onConfirm: () -> Unit,
    confirmText: String,
    cancelText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    HabitJourneyDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        message = message,
        dialogType = HabitJourneyDialogType.CONFIRMATION,
        icon = icon,
        confirmButtonText = confirmText,
        dismissButtonText = cancelText,
        onConfirm = onConfirm
    )
}