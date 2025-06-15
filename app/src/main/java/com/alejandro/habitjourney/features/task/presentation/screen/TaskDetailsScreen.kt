package com.alejandro.habitjourney.features.task.presentation.screen

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
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
import com.alejandro.habitjourney.core.utils.formatter.DateTimeFormatters


/**
 * Pantalla de detalles de la tarea.
 *
 * Muestra la información completa de una tarea específica, incluyendo su título, descripción,
 * fechas, prioridad, recordatorio y estado. Permite al usuario alternar el estado de completado,
 * editar la tarea, archivarla/desarchivarla y eliminarla.
 *
 * @param taskId El ID de la tarea a mostrar.
 * @param onNavigateBack Lambda para navegar de vuelta a la pantalla anterior.
 * @param onNavigateToEdit Lambda para navegar a la pantalla de edición de la tarea, pasando el ID de la tarea.
 * @param viewModel La instancia de [TaskDetailsViewModel] inyectada por Hilt.
 */
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
    val context = LocalContext.current // Obtener el contexto para el formateador

    // Inicializar el ViewModel con el ID de la tarea cuando el componente se compone por primera vez.
    LaunchedEffect(taskId) {
        viewModel.initializeWithTaskId(taskId)
    }

    // Estados para controlar la visibilidad de los diálogos de confirmación y el menú desplegable.
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }
    var showMenuDropdown by remember { mutableStateOf(false) }

    // Efecto para navegar hacia atrás si la tarea no existe o hay un error de carga.
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
                        style = Typography.headlineMedium
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
                    // Muestra las acciones de editar y menú solo si la tarea se ha cargado y existe.
                    if (task != null && !uiState.isLoading) {
                        // Botón para navegar a la pantalla de edición de la tarea.
                        IconButton(
                            onClick = { onNavigateToEdit(taskId) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit_task)
                            )
                        }

                        // Menú desplegable con opciones adicionales (archivar/desarchivar, eliminar).
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
                                    // Opción para archivar o desarchivar la tarea.
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
                                                showMenuDropdown = true
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

                                    // Opción para eliminar la tarea.
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
            // Contenido principal de los detalles de la tarea.
            task?.let { currentTask ->
                TaskDetailsContent(
                    task = currentTask,
                    onToggleCompletion = viewModel::toggleTaskCompletion,
                    isProcessing = uiState.isProcessing,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    context = context // Pasar el contexto al contenido para los formateadores
                )
            }

            // Muestra un overlay de carga mientras los datos se están obteniendo o procesando.
            if (uiState.isLoading) {
                HabitJourneyLoadingOverlay()
            }
        }
    }

    // Diálogo de confirmación para eliminar la tarea.
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

    // Diálogo de confirmación para archivar/desarchivar la tarea.
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

/**
 * Contenido principal de la pantalla de detalles de la tarea.
 * Organiza la información de la tarea en secciones desplazables.
 *
 * @param task La [Task] a mostrar.
 * @param onToggleCompletion Lambda para alternar el estado de completado de la tarea.
 * @param isProcessing Indica si hay una operación de completado/incompletado en curso.
 * @param context El contexto para formatear fechas y horas.
 * @param modifier Modificador para aplicar al diseño.
 */
@Composable
private fun TaskDetailsContent(
    task: Task,
    onToggleCompletion: () -> Unit,
    isProcessing: Boolean,
    context: Context,
    modifier: Modifier = Modifier
) {
    // Obtiene la fecha actual para determinar si la tarea está vencida.
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val isOverdue = task.dueDate?.let { it < now } == true && !task.isCompleted

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(Dimensions.SpacingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingLarge)
    ) {
        // Sección de encabezado que muestra el título y el checkbox de completado.
        TaskHeaderSection(
            task = task,
            onToggleCompletion = onToggleCompletion,
            isProcessing = isProcessing,
            isOverdue = isOverdue
        )

        // Sección de descripción, solo visible si la descripción no está vacía.
        if (!task.description.isNullOrBlank()) {
            TaskDescriptionSection(description = task.description)
        }

        // Sección que muestra las fechas relevantes de la tarea (vencimiento, completado, creación).
        TaskDatesSection(
            task = task,
            isOverdue = isOverdue,
            context = context
        )

        // Sección de prioridad, solo visible si la tarea tiene una prioridad asignada.
        task.priority?.let { priority ->
            TaskPrioritySection(priority = priority)
        }

        // Sección de recordatorio, solo visible si un recordatorio está establecido.
        if (task.isReminderSet && task.reminderDateTime != null) {
            TaskReminderSection(
                reminderDateTime = task.reminderDateTime,
                context = context
            )
        }

        // Sección que muestra el estado actual de la tarea (activo, completado, archivado).
        TaskStatusSection(task = task)
    }
}

/**
 * Sección del encabezado para la pantalla de detalles de la tarea.
 * Muestra el título de la tarea y un checkbox para alternar su estado de completado.
 * También indica si la tarea está vencida.
 *
 * @param task La [Task] a mostrar.
 * @param onToggleCompletion Lambda a ejecutar cuando se alterna el checkbox de completado.
 * @param isProcessing Indica si una operación de completado/incompletado está en curso.
 * @param isOverdue Indica si la tarea está vencida.
 * @param modifier Modificador para aplicar al diseño.
 */
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
                // Muestra un CircularProgressIndicator si la operación está en curso, de lo contrario, el Checkbox.
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
                    style = Typography.headlineSmall.copy(
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

                // Muestra un indicador visual si la tarea está vencida.
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
                            style = Typography.bodySmall.copy(
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

/**
 * Sección para mostrar la descripción de la tarea.
 * Solo se muestra si la tarea tiene una descripción no nula o no vacía.
 *
 * @param description La descripción de la tarea.
 * @param modifier Modificador para aplicar al diseño.
 */
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
                style = Typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

        Text(
            text = description,
            style = Typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Sección para mostrar las fechas relevantes de la tarea.
 * Incluye la fecha de vencimiento, la fecha de finalización (si aplica) y la fecha de creación.
 *
 * @param task La [Task] de la cual se obtendrán las fechas.
 * @param isOverdue Indica si la tarea está vencida para resaltar la fecha de vencimiento.
 * @param context El contexto para formatear las fechas.
 * @param modifier Modificador para aplicar al diseño.
 */
@Composable
private fun TaskDatesSection(
    task: Task,
    isOverdue: Boolean,
    context: Context,
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
                style = Typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

        // Fila de información para la fecha de vencimiento.
        task.dueDate?.let { dueDate ->
            DateInfoRow(
                label = stringResource(R.string.due_date),
                date = DateTimeFormatters.formatDateRelatively(dueDate, context),
                isError = isOverdue
            )
        }

        // Fila de información para la fecha de finalización
        task.completionDate?.let { completionDate ->
            DateInfoRow(
                label = stringResource(R.string.completion_date),
                date = DateTimeFormatters.formatDateRelatively(completionDate, context),
                isError = false
            )
        }

        // Fila de información para la fecha de creación.
        val createdDate = Instant.fromEpochMilliseconds(task.createdAt)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        DateInfoRow(
            label = stringResource(R.string.created_date),
            date = DateTimeFormatters.formatDateRelatively(createdDate, context),
            isError = false
        )
    }
}

/**
 * Fila de Composable reutilizable para mostrar un par etiqueta-fecha.
 *
 * @param label La etiqueta descriptiva de la fecha (ej. "Fecha de vencimiento").
 * @param date La fecha formateada como String para mostrar.
 * @param isError Indica si la fecha debe mostrarse con un color de error.
 * @param modifier Modificador para aplicar al diseño.
 */
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
            style = Typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = date,
            style = Typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = if (isError) Error else MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Sección para mostrar la prioridad de la tarea.
 * Incluye un icono, la etiqueta "Prioridad" y el valor de la prioridad con su indicador visual.
 *
 * @param priority La [Priority] de la tarea.
 * @param modifier Modificador para aplicar al diseño.
 */
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
                style = Typography.headlineSmall.copy(
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
                    style = Typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

/**
 * Sección para mostrar la información del recordatorio de la tarea.
 * Solo se muestra si hay un recordatorio establecido.
 *
 * @param reminderDateTime La [LocalDateTime] del recordatorio.
 * @param context El contexto para formatear la fecha y hora.
 * @param modifier Modificador para aplicar al diseño.
 */
@Composable
private fun TaskReminderSection(
    reminderDateTime: LocalDateTime,
    context: Context,
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
                style = Typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = DateTimeFormatters.formatDateTimeLocalized(reminderDateTime),
                style = Typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

/**
 * Sección para mostrar el estado general de la tarea (Activa, Completada, Archivada).
 *
 * @param task La [Task] cuyo estado se va a mostrar.
 * @param modifier Modificador para aplicar al diseño.
 */
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
                style = Typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

        // Fila de información que muestra el estado de la tarea con un color indicativo.
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

/**
 * Fila de Composable reutilizable para mostrar un par etiqueta-valor de estado.
 *
 * @param label La etiqueta descriptiva del estado (ej. "Estado").
 * @param value El valor del estado como String (ej. "Activa", "Completada").
 * @param color El color con el que se mostrará el valor del estado.
 * @param modifier Modificador para aplicar al diseño.
 */
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
            style = Typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = Typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = color
        )
    }
}

/**
 * Diálogo de confirmación genérico para acciones que requieren verificación del usuario.
 * Utiliza el componente [HabitJourneyDialog] para su estructura.
 *
 * @param onDismissRequest Lambda a ejecutar cuando el diálogo se descarta (ej. clic fuera o botón de cancelar).
 * @param title El título del diálogo.
 * @param message El mensaje principal del diálogo que explica la acción.
 * @param onConfirm Lambda a ejecutar cuando el usuario confirma la acción.
 * @param confirmText El texto para el botón de confirmación.
 * @param cancelText El texto para el botón de cancelar.
 * @param icon El icono a mostrar en el diálogo.
 */
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