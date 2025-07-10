package com.alejandro.habitjourney.features.task.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.Priority
import com.alejandro.habitjourney.core.utils.logging.AppLogger
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.task.data.local.AlarmPermissionHelper
import com.alejandro.habitjourney.features.task.data.local.PermissionType
import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.task.domain.usecase.*
import com.alejandro.habitjourney.features.task.presentation.state.CreateEditTaskUiState
import com.alejandro.habitjourney.features.user.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject


/**
 * ViewModel para la pantalla de creación y edición de tareas.
 *
 * Se encarga de gestionar el estado de la UI ([CreateEditTaskUiState]), interactuar
 * con los casos de uso para operaciones de tareas (crear, actualizar, obtener)
 * y manejar la lógica relacionada con los recordatorios y permisos de alarma.
 *
 * @property createTaskUseCase Caso de uso para crear una nueva tarea.
 * @property updateTaskUseCase Caso de uso para actualizar una tarea existente.
 * @property getTaskByIdUseCase Caso de uso para obtener una tarea por su ID.
 * @property scheduleReminderUseCase Caso de uso para programar un recordatorio.
 * @property cancelReminderUseCase Caso de uso para cancelar un recordatorio.
 * @property updateReminderUseCase Caso de uso para actualizar un recordatorio.
 * @property userPreferences Preferencias de usuario para obtener el ID del usuario actual.
 * @property alarmPermissionHelper Ayudante para gestionar los permisos de alarma.
 * @property resourceProvider Proveedor de recursos para obtener cadenas localizadas.
 */
@HiltViewModel
class CreateEditTaskViewModel @Inject constructor(
    private val createTaskUseCase: CreateTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val scheduleReminderUseCase: ScheduleReminderUseCase,
    private val cancelReminderUseCase: CancelReminderUseCase,
    private val updateReminderUseCase: UpdateReminderUseCase,
    private val userPreferences: UserPreferences,
    private val alarmPermissionHelper: AlarmPermissionHelper,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    companion object {
        private const val VM_NAME = "CreateEditTaskVM"
    }

    private val _uiState = MutableStateFlow(CreateEditTaskUiState())
    val uiState: StateFlow<CreateEditTaskUiState> = _uiState.asStateFlow()

    /**
     * Inicializa el ViewModel con el ID de la tarea y el modo de lectura.
     * Si se proporciona un `taskId`, carga los datos de la tarea existente.
     *
     * @param taskId El ID de la tarea a cargar/editar. `null` para crear una nueva tarea.
     * @param isReadOnly Si `true`, la pantalla estará en modo de solo lectura.
     */
    fun initializeTask(taskId: Long?, isReadOnly: Boolean = false) {

        val needsPermission = alarmPermissionHelper.needsPermissionRequest()

        _uiState.value = _uiState.value.copy(
            taskId = taskId,
            isReadOnly = isReadOnly,
            isLoading = taskId != null,
            needsAlarmPermission = needsPermission
        )

        if (taskId != null) {
            loadTask(taskId)
        }
    }

    /**
     * Carga una tarea existente desde el repositorio basándose en su ID.
     * Actualiza el estado de la UI con los datos de la tarea cargada.
     *
     * @param taskId El ID de la tarea a cargar.
     */
    private fun loadTask(taskId: Long) {
        AppLogger.vm(VM_NAME, "Cargando tarea con ID: $taskId")

        viewModelScope.launch {
            try {
                getTaskByIdUseCase(taskId).collect { task ->
                    AppLogger.vm(VM_NAME, "Task loaded: $task")
                    if (task != null) {
                        _uiState.value = _uiState.value.copy(
                            title = task.title,
                            description = task.description ?: "",
                            dueDate = task.dueDate,
                            priority = task.priority,
                            reminderDateTime = task.reminderDateTime,
                            isReminderEnabled = task.isReminderSet,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Actualiza el título de la tarea en el estado de la UI y valida su contenido.
     * Establece `titleError` si el título está en blanco.
     *
     * @param title El nuevo título de la tarea.
     */
    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(
            title = title,
            titleError = if (title.isBlank()) {
                resourceProvider.getString(R.string.title_required)
            } else null
        )
    }

    /**
     * Actualiza la descripción de la tarea en el estado de la UI.
     *
     * @param description La nueva descripción de la tarea.
     */
    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    /**
     * Actualiza la fecha de vencimiento de la tarea en el estado de la UI.
     *
     * @param date La nueva fecha de vencimiento, o `null` para eliminarla.
     */
    fun updateDueDate(date: LocalDate?) {
        _uiState.value = _uiState.value.copy(dueDate = date)
    }

    /**
     * Actualiza la prioridad de la tarea en el estado de la UI.
     *
     * @param priority La nueva prioridad, o `null` para eliminarla.
     */
    fun updatePriority(priority: Priority?) {
        _uiState.value = _uiState.value.copy(priority = priority)
    }

    /**
     * Actualiza el estado de habilitación del recordatorio.
     * Si se intenta habilitar el recordatorio y faltan permisos, muestra el diálogo de permisos.
     *
     * @param enabled `true` para habilitar el recordatorio, `false` para deshabilitarlo.
     */
    fun updateReminderEnabled(enabled: Boolean) {
        AppLogger.vm(VM_NAME, "updateReminderEnabled: $enabled")

        if (enabled) {
            val missingPermissions = alarmPermissionHelper.getMissingPermissions()

            if (missingPermissions.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    showPermissionDialog = true,
                    missingPermissions = missingPermissions
                )
                return
            }
        }

        _uiState.value = _uiState.value.copy(
            isReminderEnabled = enabled,
            reminderDateTime = if (!enabled) null else _uiState.value.reminderDateTime
        )
    }

    /**
     * Revalida los permisos necesarios para las alarmas y notificaciones.
     * Actualiza el estado de la UI si hay permisos faltantes.
     */
    fun revalidatePermissions() {
        val missingPermissions = alarmPermissionHelper.getMissingPermissions()

        _uiState.value = _uiState.value.copy(
            needsAlarmPermission = missingPermissions.isNotEmpty(),
            missingPermissions = missingPermissions
        )

        if (missingPermissions.isEmpty() && _uiState.value.reminderDateTime != null) {
            _uiState.value = _uiState.value.copy(
                isReminderEnabled = true
            )
        }
    }

    /**
     * Actualiza la fecha y hora del recordatorio en el estado de la UI.
     * Si se selecciona una fecha y hora, se habilita automáticamente el recordatorio.
     *
     * @param dateTime La nueva fecha y hora del recordatorio, o `null` para eliminarla.
     */
    fun updateReminderDateTime(dateTime: LocalDateTime?) {

        if (dateTime != null) {
            val missingPermissions = alarmPermissionHelper.getMissingPermissions()

            if (missingPermissions.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    showPermissionDialog = true,
                    missingPermissions = missingPermissions,
                    reminderDateTime = dateTime
                )
                return
            }
        }

        _uiState.value = _uiState.value.copy(
            reminderDateTime = dateTime,
            isReminderEnabled = dateTime != null
        )
    }

    /**
     * Maneja el resultado de la interacción con el diálogo de permisos.
     * Navega a la configuración del sistema para solicitar el permiso correspondiente.
     *
     * @param permissionType El tipo de permiso seleccionado por el usuario.
     */
    fun onPermissionDialogResult(permissionType: PermissionType) {

        _uiState.value = _uiState.value.copy(
            showPermissionDialog = false
        )

        when (permissionType) {
            PermissionType.EXACT_ALARM -> {
                alarmPermissionHelper.requestExactAlarmPermission()
            }
            PermissionType.NOTIFICATION_PERMISSION,
            PermissionType.NOTIFICATION_SETTINGS -> {
                alarmPermissionHelper.requestNotificationSettings()
            }
        }
    }

    /**
     * Maneja el cierre del diálogo de permisos, limpiando los estados relacionados.
     */
    fun onPermissionDialogDismiss() {
        _uiState.value = _uiState.value.copy(
            showPermissionDialog = false,
            missingPermissions = emptyList()
        )
    }

    /**
     * Guarda o actualiza la tarea en la base de datos.
     * Realiza validaciones, gestiona los permisos de recordatorio y programa/cancela recordatorios.
     *
     * @param onSuccess Lambda que se invoca tras guardar la tarea exitosamente.
     */
    fun saveTask(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (!state.isFormValid) {
            _uiState.value = state.copy(
                titleError = if (state.title.isBlank()) {
                    resourceProvider.getString(R.string.title_required)
                } else null
            )
            return
        }

        if (state.isReminderEnabled &&
            state.reminderDateTime != null &&
            alarmPermissionHelper.needsPermissionRequest()) {

            _uiState.value = state.copy(
                showPermissionDialog = true,
                error = resourceProvider.getString(R.string.user_not_found),
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = state.copy(isSaving = true, error = null)

                val userId = userPreferences.getCurrentUserId().first()

                if (userId == null) {
                    _uiState.value = state.copy(
                        error = resourceProvider.getString(R.string.user_not_found),
                        isSaving = false
                    )
                    return@launch
                }

                val task = if (state.taskId != null) {
                    Task(
                        id = state.taskId,
                        userId = userId,
                        title = state.title.trim(),
                        description = state.description.takeIf { it.isNotBlank() },
                        dueDate = state.dueDate,
                        priority = state.priority,
                        reminderDateTime = if (state.isReminderEnabled) state.reminderDateTime else null,
                        isReminderSet = state.isReminderEnabled,
                        createdAt = Clock.System.now().toEpochMilliseconds(),
                        isCompleted = false,
                        completionDate = null,
                        isArchived = false
                    )
                } else {
                    Task(
                        id = 0L,
                        userId = userId,
                        title = state.title.trim(),
                        description = state.description.takeIf { it.isNotBlank() },
                        dueDate = state.dueDate,
                        priority = state.priority,
                        reminderDateTime = if (state.isReminderEnabled) state.reminderDateTime else null,
                        isReminderSet = state.isReminderEnabled,
                        createdAt = Clock.System.now().toEpochMilliseconds(),
                        isCompleted = false,
                        completionDate = null,
                        isArchived = false
                    )
                }

                AppLogger.vm(VM_NAME, "Task to save: $task")

                val taskId = if (state.taskId != null) {
                    updateTaskUseCase(task)
                    AppLogger.vm(VM_NAME, "Task updated")
                    state.taskId
                } else {
                    val newTaskId = createTaskUseCase(task)
                    AppLogger.vm(VM_NAME, "Task created with ID: $newTaskId")
                    newTaskId
                }

                if (state.isReminderEnabled &&
                    state.reminderDateTime != null &&
                    alarmPermissionHelper.canScheduleExactAlarms()) {
                    AppLogger.vm(VM_NAME, "Scheduling reminder for task $taskId")
                    try {
                        if (state.taskId != null) {
                            updateReminderUseCase(
                                taskId,
                                state.reminderDateTime,
                                state.title.trim()
                            )
                        } else {
                            scheduleReminderUseCase(
                                taskId,
                                state.reminderDateTime,
                                state.title.trim()
                            )
                        }
                    } catch (e: Exception) {
                        AppLogger.e(VM_NAME, "Error programming reminder", e)
                        _uiState.value = _uiState.value.copy(
                            error = resourceProvider.getString(
                                R.string.task_saved_reminder_error,
                                e.message ?: resourceProvider.getString(R.string.unknown_error)
                            )
                        )
                    }
                } else if (state.taskId != null) {
                    AppLogger.vm(VM_NAME, "Canceling reminder for task $taskId")
                    cancelReminderUseCase(taskId)
                }

                onSuccess()

            } catch (e: Exception) {
                AppLogger.e(VM_NAME, "Error saving task", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: resourceProvider.getString(R.string.unknown_error),
                    isSaving = false
                )
            }
        }
    }

    /**
     * Limpia cualquier mensaje de error actual en el estado de la UI.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}