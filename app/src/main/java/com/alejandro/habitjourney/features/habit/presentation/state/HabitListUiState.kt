package com.alejandro.habitjourney.features.habit.presentation.state

import com.alejandro.habitjourney.features.habit.presentation.viewmodel.HabitListItemUiModel


/**
 * Define los tipos de filtro disponibles para la lista de hábitos.
 */
enum class HabitFilterType {
    TODAY,
    ALL,
    ARCHIVED,
    COMPLETED,
    PENDING
}

/**
 * Representa el estado de la UI para la pantalla que lista los hábitos.
 *
 * Contiene la lista de hábitos a mostrar, el estado actual de los filtros y la búsqueda,
 * y las banderas para gestionar la carga y los errores en la interfaz de usuario.
 *
 * @property todayHabits La lista completa de hábitos que corresponden al día de hoy, sin filtrar.
 * @property filteredHabits La lista de hábitos que se muestra actualmente en la UI, después de aplicar el filtro y la búsqueda.
 * @property currentFilter El [HabitFilterType] actualmente seleccionado por el usuario.
 * @property searchQuery El término de búsqueda introducido por el usuario.
 * @property isSearchActive `true` si la barra de búsqueda está activa y visible.
 * @property isLoading `true` si se están cargando los hábitos desde la fuente de datos.
 * @property error Un mensaje de error para mostrar al usuario, o `null` si no hay error.
 */
data class HabitListUiState(
    val todayHabits: List<HabitListItemUiModel> = emptyList(),
    val filteredHabits: List<HabitListItemUiModel> = emptyList(),
    val currentFilter: HabitFilterType = HabitFilterType.TODAY,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)


/**
 * Contenedor de datos para las listas maestras de hábitos.
 *
 * Separa los datos brutos recuperados del repositorio de los datos filtrados
 * que se muestran en la UI.
 *
 * @property allUserHabits La lista completa de todos los hábitos del usuario (activos y archivados).
 * @property todayHabits La lista de hábitos que corresponden al día de hoy.
 */
data class HabitsData(
    val allUserHabits: List<HabitListItemUiModel> = emptyList(),
    val todayHabits: List<HabitListItemUiModel> = emptyList()
)

