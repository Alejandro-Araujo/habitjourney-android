package com.alejandro.habitjourney.features.note.presentation.state

import com.alejandro.habitjourney.core.data.local.enums.NoteType
import com.alejandro.habitjourney.features.note.domain.model.NoteListItem


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
    val isAddingNewItem: Boolean = false
)  {
    val isEmpty: Boolean get() = when (noteType) {
        NoteType.TEXT -> title.isBlank() && content.isBlank()
        NoteType.LIST -> title.isBlank() && listItems.isEmpty()
    }

    val canSave: Boolean get() = !isEmpty && !isSaving
}