package com.alejandro.habitjourney.features.task.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButton
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButtonType
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyLoadingOverlay
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import com.alejandro.habitjourney.features.task.presentation.components.*
import com.alejandro.habitjourney.features.task.presentation.viewmodel.CreateEditTaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditTaskScreen(
    taskId: Long? = null,
    isReadOnly: Boolean = false,
    onNavigateBack: () -> Unit,
    viewModel: CreateEditTaskViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.revalidatePermissions()
    }

    // Inicializar el ViewModel
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

    // Manejar errores
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
                    if (!isReadOnly && !uiState.isLoading) {
                        TextButton(
                            onClick = {
                                viewModel.saveTask(onSuccess = onNavigateBack)
                            },
                            enabled = uiState.isFormValid && !uiState.isSaving
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = AcentoInformativo
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.save),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = AcentoInformativo
                                )
                            }
                        }
                    }
                }
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
                // Campo de título
                TaskTitleField(
                    title = uiState.title,
                    onTitleChange = viewModel::updateTitle,
                    error = uiState.titleError,
                    enabled = !isReadOnly
                )

                // Campo de descripción
                TaskDescriptionField(
                    description = uiState.description,
                    onDescriptionChange = viewModel::updateDescription,
                    enabled = !isReadOnly
                )

                // Selector de fecha de vencimiento
                TaskDateSelector(
                    selectedDate = uiState.dueDate,
                    onDateChange = viewModel::updateDueDate,
                    enabled = !isReadOnly,
                    label = stringResource(R.string.due_date),
                    placeholder = stringResource(R.string.select_date)
                )

                // Selector de prioridad
                TaskPrioritySelector(
                    priority = uiState.priority,
                    onPriorityChange = viewModel::updatePriority,
                    enabled = !isReadOnly
                )

                // Sección de recordatorio
                TaskReminderToggle(
                    isReminderEnabled = uiState.isReminderEnabled,
                    reminderDateTime = uiState.reminderDateTime,
                    onReminderEnabledChange = viewModel::updateReminderEnabled,
                    onReminderDateTimeChange = viewModel::updateReminderDateTime,
                    enabled = !isReadOnly
                )

                // Botón de guardar principal usando tu componente general
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
                        }
                    )
                }
            }

            // Overlay de carga usando tu componente general
            if (uiState.isLoading) {
                HabitJourneyLoadingOverlay()
            }
        }
    }
}