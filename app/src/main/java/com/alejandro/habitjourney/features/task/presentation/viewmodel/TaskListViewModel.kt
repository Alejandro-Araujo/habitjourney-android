package com.alejandro.habitjourney.features.task.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.core.data.local.enums.Priority
import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.task.domain.usecase.*
import com.alejandro.habitjourney.features.task.presentation.state.TaskFilterType
import com.alejandro.habitjourney.features.task.presentation.state.TaskListUiState
import com.alejandro.habitjourney.features.user.data.local.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * ViewModel para la pantalla de lista de tareas.
 *
 * Gestiona el estado de la UI para la visualización de tareas, incluyendo filtrado,
 * búsqueda y operaciones sobre tareas individuales como completar, archivar y eliminar.
 * Interactúa con los casos de uso de la capa de dominio y las preferencias de usuario.
 *
 * @property getAllTasksUseCase Caso de uso para obtener todas las tareas no archivadas.
 * @property getActiveTasksUseCase Caso de uso para obtener tareas activas.
 * @property getCompletedTasksUseCase Caso de uso para obtener tareas completadas.
 * @property getArchivedTasksUseCase Caso de uso para obtener tareas archivadas.
 * @property getOverdueTasksUseCase Caso de uso para obtener tareas vencidas.
 * @property toggleTaskCompletionUseCase Caso de uso para alternar el estado de completado de una tarea.
 * @property archiveTaskUseCase Caso de uso para archivar/desarchivar una tarea.
 * @property deleteTaskUseCase Caso de uso para eliminar una tarea.
 * @property userPreferences Preferencias de usuario para obtener el ID del usuario actual.
 */
@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val getAllTasksUseCase: GetAllTasksUseCase,
    private val getActiveTasksUseCase: GetActiveTasksUseCase,
    private val getCompletedTasksUseCase: GetCompletedTasksUseCase,
    private val getArchivedTasksUseCase: GetArchivedTasksUseCase,
    private val getOverdueTasksUseCase: GetOverdueTasksUseCase,
    private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase,
    private val archiveTaskUseCase: ArchiveTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _currentFilter = MutableStateFlow(TaskFilterType.ACTIVE)
    val currentFilter: StateFlow<TaskFilterType> = _currentFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedPriority = MutableStateFlow<Priority?>(null)

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val _isSearchActive = MutableStateFlow(false)

    private val _baseUiState = combine(
        _currentFilter,
        _searchQuery,
        _selectedPriority,
        _isLoading,
        _error
    ) { filter, search, priority, loading, error ->
        TaskListUiState(
            currentFilter = filter,
            searchQuery = search,
            selectedPriority = priority,
            isLoading = loading,
            error = error
        )
    }

    /**
     * El estado de la interfaz de usuario ([TaskListUiState]) que se expone a la vista.
     * Combina varios estados internos para proporcionar una representación completa del UI.
     */
    val uiState: StateFlow<TaskListUiState> = combine(
        _baseUiState,
        _isSearchActive,
    ) { baseState, searchActive ->
        baseState.copy(
            isSearchActive = searchActive,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskListUiState()
    )

    /**
     * Un [StateFlow] que emite la lista de tareas actualmente visible,
     * aplicando el filtro y la búsqueda seleccionados.
     * Se recalcula reactivamente a los cambios en el filtro actual, la consulta de búsqueda,
     * la prioridad seleccionada y el ID del usuario.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<List<Task>> =
        combine(
            _currentFilter,
            userPreferences.getCurrentUserId()
        ) { filter, userId ->
            if (userId != null) {
                when (filter) {
                    TaskFilterType.ACTIVE -> getActiveTasksUseCase(userId)
                    TaskFilterType.COMPLETED -> getCompletedTasksUseCase(userId)
                    TaskFilterType.ARCHIVED -> getArchivedTasksUseCase(userId)
                    TaskFilterType.OVERDUE -> getOverdueTasksUseCase(userId)
                    TaskFilterType.ALL ->  getAllTasksUseCase(userId)
                }
            } else {
                flowOf(emptyList())
            }
        }.flatMapLatest { it }
            .combine(_searchQuery) { taskList, query ->
                if (query.isBlank()) {
                    taskList
                } else {
                    taskList.filter { task ->
                        task.title.contains(query, ignoreCase = true) ||
                                task.description?.contains(query, ignoreCase = true) == true
                    }
                }
            }
            .combine(_selectedPriority) { taskList, priority ->
                if (priority == null) {
                    taskList
                } else {
                    taskList.filter { it.priority == priority }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    /**
     * Establece el filtro actual para la lista de tareas.
     *
     * @param filter El nuevo [TaskFilterType] a aplicar.
     */
    fun setFilter(filter: TaskFilterType) {
        _currentFilter.value = filter
    }

    /**
     * Establece la consulta de búsqueda para filtrar tareas por título o descripción.
     *
     * @param query La cadena de texto a usar para la búsqueda.
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Alterna el estado de la barra de búsqueda (activa/inactiva).
     * Si se desactiva la búsqueda, la consulta de búsqueda se limpia.
     */
    fun toggleSearch() {
        _isSearchActive.value = !_isSearchActive.value
        if (!_isSearchActive.value) {
            _searchQuery.value = ""
        }
    }

    /**
     * Alterna el estado de completado de una tarea específica.
     * Si ocurre un error, actualiza el estado de error de la UI.
     *
     * @param taskId El ID de la tarea a actualizar.
     * @param isCompleted El nuevo estado de completado (`true` para completada, `false` para incompleta).
     */
    fun toggleTaskCompletion(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                toggleTaskCompletionUseCase(taskId, isCompleted)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /**
     * Archiva una tarea específica.
     * Si ocurre un error, actualiza el estado de error de la UI.
     *
     * @param taskId El ID de la tarea a archivar.
     */
    fun archiveTask(taskId: Long) {
        viewModelScope.launch {
            try {
                archiveTaskUseCase(taskId, true)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /**
     * Desarchiva una tarea específica.
     * Si ocurre un error, actualiza el estado de error de la UI.
     *
     * @param taskId El ID de la tarea a desarchivar.
     */
    fun unarchiveTask(taskId: Long) {
        viewModelScope.launch {
            try {
                archiveTaskUseCase(taskId, false)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /**
     * Elimina una tarea específica de forma permanente.
     * Si ocurre un error, actualiza el estado de error de la UI.
     *
     * @param taskId El ID de la tarea a eliminar.
     */
    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            try {
                deleteTaskUseCase(taskId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /**
     * Limpia cualquier mensaje de error actual en el estado de la UI.
     */
    fun clearError() {
        _error.value = null
    }
}