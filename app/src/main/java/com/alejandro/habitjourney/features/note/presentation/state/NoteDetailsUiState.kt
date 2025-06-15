package com.alejandro.habitjourney.features.note.presentation.state

/**
 * Representa el estado de la UI para la pantalla de detalle de una nota.
 *
 * Contiene las banderas de estado necesarias para gestionar la carga,
 * las operaciones en segundo plano y los errores de la pantalla.
 *
 * @property isLoading `true` si los datos iniciales de la nota se están cargando.
 * @property isProcessing `true` si se está ejecutando una acción (ej: archivar, eliminar).
 * @property noteExists `false` si la nota no se pudo encontrar (ej: fue eliminada).
 * @property error Un mensaje de error para mostrar al usuario, o `null` si no hay error.
 */
data class NoteDetailsUiState(
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val noteExists: Boolean = true,
    val error: String? = null
)