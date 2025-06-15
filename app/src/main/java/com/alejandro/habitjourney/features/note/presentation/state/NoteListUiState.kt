package com.alejandro.habitjourney.features.note.presentation.state

import com.alejandro.habitjourney.features.note.domain.model.Note

/**
 * Define los tipos de filtro disponibles para la lista de notas.
 */
enum class NoteFilterType {
    ACTIVE,
    ALL,
    ARCHIVED,
    FAVORITES
}

/**
 * Representa el estado de la UI para la pantalla que lista las notas.
 *
 * @property notes La lista de notas a mostrar después de aplicar los filtros y la búsqueda.
 * @property isLoading `true` si se están cargando las notas.
 * @property error Un mensaje de error, o `null` si no hay ninguno.
 * @property currentFilter El filtro actualmente seleccionado.
 * @property searchQuery El término de búsqueda actual.
 * @property isSearchActive `true` si la barra de búsqueda está activa.
 */
data class NoteListUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentFilter: NoteFilterType = NoteFilterType.ACTIVE,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
)