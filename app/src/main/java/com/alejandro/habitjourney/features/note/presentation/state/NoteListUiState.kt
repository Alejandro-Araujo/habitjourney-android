package com.alejandro.habitjourney.features.note.presentation.state


import com.alejandro.habitjourney.features.note.domain.model.Note

data class NoteListUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentFilter: NoteFilterType = NoteFilterType.ACTIVE,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
)