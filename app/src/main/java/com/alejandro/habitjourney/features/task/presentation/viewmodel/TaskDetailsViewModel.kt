package com.alejandro.habitjourney.features.task.presentation.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.task.domain.usecase.*
import com.alejandro.habitjourney.features.task.presentation.state.TaskDetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailsViewModel @Inject constructor(
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase,
    private val archiveTaskUseCase: ArchiveTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val cancelReminderUseCase: CancelReminderUseCase
) : ViewModel() {

    private val _taskId = MutableStateFlow<Long?>(null)

    private val _uiState = MutableStateFlow(TaskDetailsUiState())
    val uiState: StateFlow<TaskDetailsUiState> = _uiState.asStateFlow()

    // Observar la tarea
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

    @androidx.annotation.OptIn(UnstableApi::class)
    fun initializeWithTaskId(taskId: Long) {
        Log.d("TaskDetailsVM", "🔍 Inicializando con taskId: $taskId")

        // Validar taskId
        if (taskId <= 0) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                taskExists = false,
                error = "ID de tarea inválido"
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
                Log.d("TaskDetailsVM", "🔍 Task actualizada: $taskData")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    taskExists = taskData != null,
                    error = if (taskData == null) "Tarea no encontrada" else null
                )
            }
        }
    }

    fun toggleTaskCompletion() {
        val currentTask = task.value ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isProcessing = true)
                toggleTaskCompletionUseCase(currentTask.id, !currentTask.isCompleted)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al actualizar la tarea",
                    isProcessing = false
                )
            } finally {
                _uiState.value = _uiState.value.copy(isProcessing = false)
            }
        }
    }

    fun archiveTask(onSuccess: () -> Unit) {
        val currentTask = task.value ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isProcessing = true)

                // Cancelar recordatorio al archivar
                if (currentTask.isReminderSet) {
                    cancelReminderUseCase(currentTask.id)
                }

                archiveTaskUseCase(currentTask.id, !currentTask.isArchived)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al archivar la tarea",
                    isProcessing = false
                )
            }
        }
    }

    fun deleteTask(onSuccess: () -> Unit) {
        val currentTask = task.value ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isProcessing = true)

                // Cancelar recordatorio antes de eliminar
                if (currentTask.isReminderSet) {
                    cancelReminderUseCase(currentTask.id)
                }

                deleteTaskUseCase(currentTask.id)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al eliminar la tarea",
                    isProcessing = false
                )
            }
        }
    }


    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}