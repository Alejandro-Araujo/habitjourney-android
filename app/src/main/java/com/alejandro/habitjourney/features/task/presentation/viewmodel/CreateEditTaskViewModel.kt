// CreateEditTaskViewModel.kt - Versión con debug y corregida
package com.alejandro.habitjourney.features.task.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.Priority
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.task.data.local.AlarmPermissionHelper
import com.alejandro.habitjourney.features.task.data.local.PermissionType
import com.alejandro.habitjourney.features.task.data.local.ReminderManager
import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.task.domain.usecase.*
import com.alejandro.habitjourney.features.task.presentation.state.CreateEditTaskUiState
import com.alejandro.habitjourney.features.user.data.local.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject

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
    private val reminderManager: ReminderManager,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    companion object {
        private const val TAG = "CreateEditTaskVM"
    }

    private val _uiState = MutableStateFlow(CreateEditTaskUiState())
    val uiState: StateFlow<CreateEditTaskUiState> = _uiState.asStateFlow()

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

    private fun loadTask(taskId: Long) {
        Log.d(TAG, "loadTask: $taskId")

        viewModelScope.launch {
            try {
                getTaskByIdUseCase(taskId).collect { task ->
                    Log.d(TAG, "Task loaded: $task")
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

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(
            title = title,
            titleError = if (title.isBlank()) {
                resourceProvider.getString(R.string.title_required)
            } else null
        )
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateDueDate(date: LocalDate?) {
        _uiState.value = _uiState.value.copy(dueDate = date)
    }

    fun updatePriority(priority: Priority?) {
        _uiState.value = _uiState.value.copy(priority = priority)
    }

    fun updateReminderEnabled(enabled: Boolean) {
        Log.d(TAG, "updateReminderEnabled: $enabled")

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

    fun onPermissionDialogResult(granted: Boolean) {

        _uiState.value = _uiState.value.copy(
            showPermissionDialog = false
        )

        if (granted) {
            alarmPermissionHelper.requestExactAlarmPermission()
        }
    }

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

    fun onPermissionDialogDismiss() {
        _uiState.value = _uiState.value.copy(
            showPermissionDialog = false,
            missingPermissions = emptyList()
        )
    }

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
                    // Actualizar tarea existente
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
                    // Crear nueva tarea
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

                Log.d(TAG, "Task to save: $task")

                // Guardar tarea
                val taskId = if (state.taskId != null) {
                    updateTaskUseCase(task)
                    Log.d(TAG, "Task updated")
                    state.taskId
                } else {
                    val newTaskId = createTaskUseCase(task)
                    Log.d(TAG, "Task created with ID: $newTaskId")
                    newTaskId
                }

                // Manejar recordatorios después de guardar la tarea
                if (state.isReminderEnabled &&
                    state.reminderDateTime != null &&
                    alarmPermissionHelper.canScheduleExactAlarms()) {
                    Log.d(TAG, "Scheduling reminder for task $taskId")
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
                        Log.e(TAG, "Error programming reminder", e)
                        _uiState.value = _uiState.value.copy(
                            error = resourceProvider.getString(
                                R.string.task_saved_reminder_error,
                                e.message ?: resourceProvider.getString(R.string.unknown_error)
                            )
                        )
                    }
                } else if (state.taskId != null) {
                    // Cancelar recordatorio si se deshabilitó
                    Log.d(TAG, "Canceling reminder for task $taskId")
                    cancelReminderUseCase(taskId)
                }

                onSuccess()

            } catch (e: Exception) {
                Log.e(TAG, "Error saving task", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: resourceProvider.getString(R.string.unknown_error),
                    isSaving = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}