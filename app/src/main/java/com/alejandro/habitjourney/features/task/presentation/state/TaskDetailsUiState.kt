package com.alejandro.habitjourney.features.task.presentation.state


data class TaskDetailsUiState(
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val taskExists: Boolean = false,
    val error: String? = null,
    val showDeleteConfirmation: Boolean = false,
    val showArchiveConfirmation: Boolean = false
)