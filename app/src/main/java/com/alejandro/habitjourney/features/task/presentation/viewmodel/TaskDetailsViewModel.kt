package com.alejandro.habitjourney.features.task.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.exception.ErrorHandler
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.task.domain.usecase.*
import com.alejandro.habitjourney.features.task.presentation.state.TaskDetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para gestionar los detalles de una tarea específica.
 *
 * Responsabilidades:
 * - Cargar y observar cambios en una tarea específica
 * - Manejar completado, archivo y eliminación de tareas
 * - Gestionar recordatorios automáticamente
 * - Proporcionar estados de carga y manejo de errores
 */
@HiltViewModel
class TaskDetailsViewModel @Inject constructor(
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase,
    private val archiveTaskUseCase: ArchiveTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val cancelReminderUseCase: CancelReminderUseCase,
    private val errorHandler: ErrorHandler,
    private val resourceProvider: ResourceProvider,
    application: Application
) : AndroidViewModel(application) {

    private val _taskId = MutableStateFlow<Long?>(null)

    private val _uiState = MutableStateFlow(TaskDetailsUiState())
    val uiState: StateFlow<TaskDetailsUiState> = _uiState.asStateFlow()

    /**
     * Observa los cambios en la tarea actual.
     * Se actualiza automáticamente cuando cambia el taskId.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val task: StateFlow<Task?> = _taskId
        .filterNotNull()
        .flatMapLatest { taskId ->
            getTaskByIdUseCase(taskId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * Inicializa el ViewModel con un ID de tarea específico.
     *
     * @param taskId ID de la tarea a cargar. Debe ser mayor a 0.
     */
    fun initializeWithTaskId(taskId: Long) {
        // Validar taskId
        if (taskId <= 0) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                taskExists = false,
                error = resourceProvider.getString(R.string.error_invalid_task_id)
            )
            return
        }

        _taskId.value = taskId
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            taskExists = false,
            error = null
        )

        // Observar cambios en la tarea
        viewModelScope.launch {
            task.collect { taskData ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    taskExists = taskData != null,
                    error = if (taskData == null) resourceProvider.getString(R.string.task_not_found) else null
                )
            }
        }
    }

    /**
     * Alterna el estado de completado de la tarea actual.
     * Actualiza automáticamente el estado en la base de datos.
     */
    fun toggleTaskCompletion() {
        val currentTask = task.value ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isProcessing = true)
                toggleTaskCompletionUseCase(currentTask.id, !currentTask.isCompleted)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = resourceProvider.getString(R.string.error_updating_task_completion, errorHandler.getErrorMessage(e)),
                    isProcessing = false
                )
            } finally {
                _uiState.value = _uiState.value.copy(isProcessing = false)
            }
        }
    }

    /**
     * Archiva o desarchivar la tarea actual.
     * Cancela automáticamente los recordatorios si están configurados.
     *
     * @param onSuccess Callback que se ejecuta cuando la operación es exitosa
     */
    fun archiveTask(onSuccess: () -> Unit) {
        val currentTask = task.value ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isProcessing = true)

                // Cancelar recordatorio al archivar si está configurado
                if (currentTask.isReminderSet) {
                    cancelReminderUseCase(currentTask.id)
                }

                archiveTaskUseCase(currentTask.id, !currentTask.isArchived)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = resourceProvider.getString(R.string.error_archiving_task, errorHandler.getErrorMessage(e)),
                    isProcessing = false
                )
            }
        }
    }

    /**
     * Elimina la tarea actual de forma permanente.
     * Cancela automáticamente los recordatorios antes de eliminar.
     *
     * @param onSuccess Callback que se ejecuta cuando la eliminación es exitosa
     */
    fun deleteTask(onSuccess: () -> Unit) {
        val currentTask = task.value ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isProcessing = true)

                // Cancelar recordatorio antes de eliminar si está configurado
                if (currentTask.isReminderSet) {
                    cancelReminderUseCase(currentTask.id)
                }

                deleteTaskUseCase(currentTask.id)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = resourceProvider.getString(R.string.error_deleting_task, errorHandler.getErrorMessage(e)),
                    isProcessing = false
                )
            }
        }
    }

    /**
     * Limpia el mensaje de error actual.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}