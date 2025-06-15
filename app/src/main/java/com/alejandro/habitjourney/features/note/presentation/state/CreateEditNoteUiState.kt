package com.alejandro.habitjourney.features.note.presentation.state

import com.alejandro.habitjourney.core.data.local.enums.NoteType
import com.alejandro.habitjourney.features.note.domain.model.NoteListItem

/**
 * Representa el estado de la UI para la pantalla de creación y edición de notas.
 *
 * Contiene todos los campos del formulario, así como los estados de carga,
 * guardado y error necesarios para gestionar la interfaz de usuario.
 *
 * @property noteId El ID de la nota que se está editando, o `null` si se está creando una nueva.
 * @property title El título actual de la nota en el editor.
 * @property content El contenido de texto actual para notas de tipo TEXT.
 * @property isFavorite `true` si la nota está marcada como favorita.
 * @property isLoading `true` si se están cargando los datos de una nota existente.
 * @property isSaving `true` si una operación de guardado está en progreso.
 * @property error Un mensaje de error para mostrar al usuario, o `null` si no hay error.
 * @property isReadOnly `true` si la pantalla está en modo de solo lectura.
 * @property hasUnsavedChanges `true` si el usuario ha realizado cambios que aún no se han guardado.
 * @property noteType El [NoteType] actual (TEXT o LIST).
 * @property listItems La lista de [NoteListItem] para notas de tipo LIST.
 * @property isAddingNewItem `true` para indicar que se debe hacer scroll y enfocar el último ítem añadido.
 */
data class CreateEditNoteUiState(
    val noteId: Long? = null,
    val title: String = "",
    val content: String = "",
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isReadOnly: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val noteType: NoteType = NoteType.TEXT,
    val listItems: List<NoteListItem> = emptyList(),
    val isAddingNewItem: Boolean = false,
    val createdAt: Long = 0L
)  {
    /**
     * Propiedad computada que determina si la nota se considera vacía.
     */
    val isEmpty: Boolean get() = when (noteType) {
        NoteType.TEXT -> title.isBlank() && content.isBlank()
        NoteType.LIST -> title.isBlank() && listItems.isEmpty()
    }

    /**
     * Propiedad computada que determina si la nota se puede guardar.
     * La nota no debe estar vacía ni en proceso de guardado.
     */
    val canSave: Boolean get() = !isEmpty && !isSaving
}
