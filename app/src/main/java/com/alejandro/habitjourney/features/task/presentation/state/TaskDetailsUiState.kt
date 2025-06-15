package com.alejandro.habitjourney.features.task.presentation.state

/**
 * Representa el estado de la interfaz de usuario para la pantalla de detalles de una tarea.
 *
 * Este estado encapsula todas las condiciones y datos de UI necesarios para mostrar
 * los detalles de una tarea, incluyendo estados de carga, procesamiento de acciones,
 * la existencia de la tarea y la visibilidad de diálogos de confirmación.
 *
 * @property isLoading Indica si los datos de la tarea están siendo cargados.
 * @property isProcessing Indica si una operación (como completar/descompletar, archivar/desarchivar) está en curso.
 * @property taskExists Indica si la tarea solicitada fue encontrada y existe en la base de datos.
 * @property error Mensaje de error a mostrar, o `null` si no hay error.
 * @property showDeleteConfirmation Indica si el diálogo de confirmación para eliminar la tarea debe mostrarse.
 * @property showArchiveConfirmation Indica si el diálogo de confirmación para archivar/desarchivar la tarea debe mostrarse.
 */
data class TaskDetailsUiState(
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val taskExists: Boolean = false,
    val error: String? = null,
    val showDeleteConfirmation: Boolean = false,
    val showArchiveConfirmation: Boolean = false
)