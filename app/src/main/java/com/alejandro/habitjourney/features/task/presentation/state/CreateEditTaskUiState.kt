package com.alejandro.habitjourney.features.task.presentation.state

import com.alejandro.habitjourney.core.data.local.enums.Priority
import com.alejandro.habitjourney.features.task.data.local.PermissionType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * Representa el estado de la interfaz de usuario para la pantalla de Crear/Editar Tarea.
 *
 * Este estado encapsula todos los datos y condiciones de UI necesarios para renderizar
 * y gestionar el formulario para crear o editar una tarea, incluyendo los campos del formulario,
 * indicadores de carga/guardado, mensajes de error y estados relacionados con permisos.
 *
 * @property taskId El ID de la tarea que se está editando. Es `null` si se está creando una nueva tarea.
 * @property title El valor actual del título de la tarea introducido por el usuario.
 * @property description El valor actual de la descripción de la tarea introducido por el usuario.
 * @property dueDate La fecha de vencimiento seleccionada actualmente para la tarea, o `null` si no está establecida.
 * @property priority La prioridad seleccionada actualmente para la tarea, o `null` si no está establecida.
 * @property reminderDateTime La fecha y hora seleccionada actualmente para el recordatorio de la tarea, o `null` si no está establecida.
 * @property isReminderEnabled Indica si la funcionalidad de recordatorio está habilitada para la tarea.
 * @property isLoading Indica si los datos de la tarea se están cargando actualmente (por ejemplo, al editar una tarea existente).
 * @property isSaving Indica si la tarea se está guardando o actualizando actualmente.
 * @property error Un mensaje de error a mostrar, o `null` si no hay error.
 * @property isReadOnly Si es `true`, el formulario se muestra en modo de solo lectura, impidiendo ediciones.
 * @property titleError Un mensaje de error específico para el campo del título, o `null` si el título es válido.
 * @property needsAlarmPermission Indica si la aplicación requiere permisos de alarma.
 * @property showPermissionDialog Si es `true`, se debe mostrar un diálogo solicitando permisos.
 * @property missingPermissions Una lista de [PermissionType]s que faltan actualmente.
 */
data class CreateEditTaskUiState(
    val taskId: Long? = null,
    val title: String = "",
    val description: String = "",
    val dueDate: LocalDate? = null,
    val priority: Priority? = null,
    val reminderDateTime: LocalDateTime? = null,
    val isReminderEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isReadOnly: Boolean = false,
    val titleError: String? = null,
    val needsAlarmPermission: Boolean = false,
    val showPermissionDialog: Boolean = false,
    val missingPermissions: List<PermissionType> = emptyList()
) {
    /**
     * Propiedad calculada que indica si el formulario de la tarea es actualmente válido para su envío.
     * El formulario se considera válido si el [title] no está en blanco y no hay un [titleError].
     */
    val isFormValid: Boolean
        get() = title.isNotBlank() && titleError == null
}