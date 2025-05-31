package com.alejandro.habitjourney.features.task.presentation.state


import com.alejandro.habitjourney.core.data.local.enums.Priority
import com.alejandro.habitjourney.features.task.data.local.PermissionType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

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
    val isFormValid: Boolean
        get() = title.isNotBlank() && titleError == null
}