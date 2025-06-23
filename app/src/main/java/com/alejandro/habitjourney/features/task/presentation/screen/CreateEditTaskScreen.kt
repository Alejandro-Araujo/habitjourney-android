package com.alejandro.habitjourney.features.task.presentation.screen

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.Priority
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButton
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButtonType
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyLoadingOverlay
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyTextField
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import com.alejandro.habitjourney.features.task.presentation.components.*
import com.alejandro.habitjourney.features.task.presentation.viewmodel.CreateEditTaskViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.alejandro.habitjourney.core.utils.formatter.DateTimeFormatters
import kotlinx.datetime.atStartOfDayIn


/**
 * Pantalla para crear o editar una tarea existente.
 *
 * Permite al usuario introducir y modificar los detalles de una tarea como el título,
 * descripción, fecha de vencimiento, prioridad y configuración de recordatorios.
 * También puede funcionar en modo de solo lectura para mostrar los detalles sin permitir la edición.
 *
 * @param taskId El ID de la tarea a editar. Si es `null`, la pantalla está en modo de creación.
 * @param isReadOnly Si `true`, la pantalla se muestra en modo de solo lectura.
 * @param onNavigateBack Lambda para navegar de vuelta a la pantalla anterior.
 * @param viewModel La instancia de [CreateEditTaskViewModel] inyectada por Hilt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditTaskScreen(
    taskId: Long? = null,
    isReadOnly: Boolean = false,
    onNavigateBack: () -> Unit,
    viewModel: CreateEditTaskViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.revalidatePermissions()
    }

    LaunchedEffect(taskId, isReadOnly) {
        viewModel.initializeTask(taskId, isReadOnly)
    }

    if (uiState.showPermissionDialog) {
        AlarmPermissionDialog(
            missingPermissions = uiState.missingPermissions,
            onPermissionSelected = { permissionType ->
                viewModel.onPermissionDialogResult(permissionType)
            },
            onDismiss = {
                viewModel.onPermissionDialogDismiss()
            }
        )
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            isReadOnly -> stringResource(R.string.task_details)
                            taskId != null -> stringResource(R.string.edit_task)
                            else -> stringResource(R.string.create_task)
                        },
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
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(Dimensions.SpacingMedium),
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingLarge)
            ) {
                TaskTitleField(
                    title = uiState.title,
                    onTitleChange = viewModel::updateTitle,
                    error = uiState.titleError,
                    enabled = !isReadOnly
                )

                TaskDescriptionField(
                    description = uiState.description,
                    onDescriptionChange = viewModel::updateDescription,
                    enabled = !isReadOnly
                )

                TaskDateSelector(
                    selectedDate = uiState.dueDate,
                    onDateChange = viewModel::updateDueDate,
                    enabled = !isReadOnly,
                    label = stringResource(R.string.due_date),
                    placeholder = stringResource(R.string.select_date),
                    context = context
                )

                TaskPrioritySelector(
                    priority = uiState.priority,
                    onPriorityChange = viewModel::updatePriority,
                    enabled = !isReadOnly
                )

                TaskReminderToggle(
                    isReminderEnabled = uiState.isReminderEnabled,
                    reminderDateTime = uiState.reminderDateTime,
                    onReminderEnabledChange = viewModel::updateReminderEnabled,
                    onReminderDateTimeChange = viewModel::updateReminderDateTime,
                    enabled = !isReadOnly,
                    context = context
                )

                if (!isReadOnly) {
                    HabitJourneyButton(
                        text = if (taskId != null) {
                            stringResource(R.string.update_task)
                        } else {
                            stringResource(R.string.create_task)
                        },
                        onClick = {
                            viewModel.saveTask(onSuccess = onNavigateBack)
                        },
                        type = HabitJourneyButtonType.PRIMARY,
                        enabled = uiState.isFormValid,
                        isLoading = uiState.isSaving,
                        leadingIcon = if (taskId != null) Icons.Default.Save else Icons.Default.Add,
                        iconContentDescription = if (taskId != null) {
                            stringResource(R.string.save)
                        } else {
                            stringResource(R.string.create_task)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (uiState.isLoading) {
                HabitJourneyLoadingOverlay()
            }
        }
    }
}

/**
 * Campo de texto para el título de la tarea.
 *
 * @param title El valor actual del título.
 * @param onTitleChange Lambda para actualizar el valor del título.
 * @param error Mensaje de error a mostrar si el título no es válido, o `null` si es válido.
 * @param enabled Si `false`, el campo de texto está deshabilitado.
 * @param modifier Modificador para aplicar al diseño.
 */
@Composable
fun TaskTitleField(
    title: String,
    onTitleChange: (String) -> Unit,
    error: String?,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    HabitJourneyTextField(
        value = title,
        onValueChange = onTitleChange,
        label = stringResource(R.string.task_title),
        placeholder = stringResource(R.string.task_title_placeholder),
        modifier = modifier,
        enabled = enabled,
        isError = error != null,
        helperText = error,
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Assignment,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.IconSizeNormal)
            )
        }
    )
}

/**
 * Campo de texto para la descripción de la tarea.
 *
 * @param description El valor actual de la descripción.
 * @param onDescriptionChange Lambda para actualizar el valor de la descripción.
 * @param enabled Si `false`, el campo de texto está deshabilitado.
 * @param modifier Modificador para aplicar al diseño.
 */
@Composable
fun TaskDescriptionField(
    description: String?,
    onDescriptionChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    HabitJourneyTextField(
        value = description ?: "",
        onValueChange = onDescriptionChange,
        label = stringResource(R.string.task_description),
        placeholder = stringResource(R.string.task_description_placeholder),
        modifier = modifier,
        enabled = enabled,
        singleLine = false,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.IconSizeNormal)
            )
        }
    )
}

/**
 * Selector de fecha para la fecha de vencimiento de la tarea.
 * Permite al usuario seleccionar una fecha usando un DatePicker.
 *
 * @param selectedDate La fecha seleccionada actualmente, o `null` si ninguna.
 * @param onDateChange Lambda para actualizar la fecha seleccionada.
 * @param enabled Si `false`, el selector está deshabilitado.
 * @param label El texto de la etiqueta para el selector de fecha.
 * @param placeholder El texto del placeholder cuando no hay fecha seleccionada.
 * @param context El contexto para formatear la fecha.
 * @param modifier Modificador para aplicar al diseño.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDateSelector(
    selectedDate: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    enabled: Boolean,
    label: String,
    placeholder: String,
    context: Context,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = Typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { if (enabled) showDatePicker = true },
                modifier = Modifier.weight(1f),
                enabled = enabled,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AcentoInformativo
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.IconSizeButton)
                )
                Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
                Text(
                    text = selectedDate?.let { DateTimeFormatters.formatDateRelatively(it, context) } ?: placeholder,
                    style = Typography.bodyLarge
                )
            }

            if (selectedDate != null && enabled) {
                IconButton(
                    onClick = { onDateChange(null) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.clear_date),
                        tint = Error
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val timeZone = TimeZone.currentSystemDefault()
        val initialMillisForPicker = remember(selectedDate) {
            selectedDate?.atStartOfDayIn(timeZone)?.toEpochMilliseconds()
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillisForPicker,
            initialDisplayedMonthMillis = initialMillisForPicker
        )
        TaskDatePickerDialog(
            datePickerState = datePickerState,
            onDateSelected = { timestamp ->
                timestamp?.let {
                    val instant = Instant.fromEpochMilliseconds(it)
                    val date = instant.toLocalDateTime(timeZone).date
                    onDateChange(date)
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

/**
 * Selector de prioridad para la tarea.
 * Permite al usuario elegir una prioridad de una lista desplegable.
 *
 * @param priority La prioridad seleccionada actualmente, o `null` si ninguna.
 * @param onPriorityChange Lambda para actualizar la prioridad seleccionada.
 * @param enabled Si `false`, el selector está deshabilitado.
 * @param modifier Modificador para aplicar al diseño.
 */
@Composable
fun TaskPrioritySelector(
    priority: Priority?,
    onPriorityChange: (Priority?) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var showPriorityMenu by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.priority),
            style = Typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { if (enabled) showPriorityMenu = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AcentoInformativo
                    )
                ) {
                    priority?.let { p ->
                        TaskPriorityIcon(priority = p)
                        Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
                    }
                    Text(
                        text = priority?.let { getPriorityText(it) }
                            ?: stringResource(R.string.select_priority),
                        style = Typography.bodyLarge
                    )
                }

                DropdownMenu(
                    expanded = showPriorityMenu,
                    onDismissRequest = { showPriorityMenu = false }
                ) {
                    Priority.entries.forEach { p ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TaskPriorityIcon(priority = p)
                                    Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
                                    Text(
                                        text = getPriorityText(p),
                                        style = Typography.bodyMedium
                                    )
                                }
                            },
                            onClick = {
                                onPriorityChange(p)
                                showPriorityMenu = false
                            }
                        )
                    }

                    if (priority != null) {
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = null,
                                        modifier = Modifier.size(Dimensions.IconSizeButton),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
                                    Text(
                                        text = stringResource(R.string.no_priority),
                                        style = Typography.bodyMedium
                                    )
                                }
                            },
                            onClick = {
                                onPriorityChange(null)
                                showPriorityMenu = false
                            }
                        )
                    }
                }
            }

            if (priority != null && enabled) {
                IconButton(
                    onClick = { onPriorityChange(null) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.clear_priority),
                        tint = Error
                    )
                }
            }
        }
    }
}

/**
 * Icono visual que representa el nivel de prioridad de una tarea.
 *
 * @param priority La [Priority] de la tarea.
 * @param modifier Modificador para aplicar al diseño.
 */
@Composable
fun TaskPriorityIcon(
    priority: Priority,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = when (priority) {
            Priority.HIGH -> Icons.Default.KeyboardArrowUp
            Priority.MEDIUM -> Icons.Default.Remove
            Priority.LOW -> Icons.Default.KeyboardArrowDown
        },
        contentDescription = null,
        modifier = modifier.size(Dimensions.IconSizeButton),
        tint = when (priority) {
            Priority.HIGH -> AcentoUrgente
            Priority.MEDIUM -> Logro
            Priority.LOW -> AcentoPositivo
        }
    )
}

/**
 * Provee el texto localizado para un valor de [Priority].
 *
 * @param priority El valor de [Priority] a traducir.
 * @return Un [String] con el texto localizado de la prioridad.
 */
@Composable
private fun getPriorityText(priority: Priority): String {
    return when (priority) {
        Priority.HIGH -> stringResource(R.string.priority_high)
        Priority.MEDIUM -> stringResource(R.string.priority_medium)
        Priority.LOW -> stringResource(R.string.priority_low)
    }
}