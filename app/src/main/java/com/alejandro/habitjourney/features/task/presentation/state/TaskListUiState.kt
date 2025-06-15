package com.alejandro.habitjourney.features.task.presentation.state

import com.alejandro.habitjourney.core.data.local.enums.Priority
import com.alejandro.habitjourney.features.task.domain.model.Task

/**
 * Representa el estado de la interfaz de usuario para la lista de tareas.
 *
 * Este estado encapsula todos los datos y condiciones de UI necesarios para mostrar
 * la lista de tareas, incluyendo la lista de tareas en sí, estados de carga y error,
 * y configuraciones de filtrado y búsqueda.
 *
 * @property tasks La lista de objetos [Task] que se muestra actualmente.
 * @property isLoading Indica si los datos de las tareas están siendo cargados.
 * @property error Mensaje de error a mostrar, o `null` si no hay error.
 * @property currentFilter El tipo de filtro de tareas aplicado actualmente a la lista.
 * @property searchQuery La cadena de texto utilizada para buscar tareas.
 * @property selectedPriority La prioridad seleccionada para filtrar, o `null` si no se aplica filtro por prioridad.
 * @property isSearchActive Indica si la barra de búsqueda está activa y visible.
 */
data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentFilter: TaskFilterType = TaskFilterType.ACTIVE,
    val searchQuery: String = "",
    val selectedPriority: Priority? = null,
    val isSearchActive: Boolean = false,
)

/**
 * Define los diferentes tipos de filtros que se pueden aplicar a la lista de tareas.
 */
enum class TaskFilterType {
    ALL,
    ACTIVE,
    COMPLETED,
    ARCHIVED,
    OVERDUE
}