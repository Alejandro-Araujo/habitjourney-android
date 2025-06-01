package com.alejandro.habitjourney.features.note.presentation.state

data class NoteDetailsUiState(
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val noteExists: Boolean = false,
    val error: String? = null
)