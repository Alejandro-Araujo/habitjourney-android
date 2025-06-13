package com.alejandro.habitjourney.features.note.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.NoteType
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions
import com.alejandro.habitjourney.features.note.domain.model.Note
import com.alejandro.habitjourney.features.note.domain.model.NoteListItem
import com.alejandro.habitjourney.features.note.domain.usecase.*
import com.alejandro.habitjourney.features.note.presentation.state.CreateEditNoteUiState
import com.alejandro.habitjourney.features.user.data.local.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject

@HiltViewModel
class CreateEditNoteViewModel @Inject constructor(
    private val createNoteUseCase: CreateNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val archiveNoteUseCase: ArchiveNoteUseCase,
    private val toggleFavoriteNoteUseCase: ToggleFavoriteNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val userPreferences: UserPreferences,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateEditNoteUiState())
    val uiState: StateFlow<CreateEditNoteUiState> = _uiState.asStateFlow()

    fun initializeNote(noteId: Long?, isReadOnly: Boolean = false) {
        _uiState.value = _uiState.value.copy(
            noteId = noteId,
            isReadOnly = isReadOnly,
            isLoading = noteId != null
        )

        if (noteId != null) {
            loadNote(noteId)
        }
    }

    private fun loadNote(noteId: Long) {
        viewModelScope.launch {
            try {
                getNoteByIdUseCase(noteId).collect { note ->
                    if (note != null) {
                        _uiState.value = _uiState.value.copy(
                            title = note.title,
                            content = note.content,
                            noteType = note.noteType,
                            listItems = note.listItems,
                            isFavorite = note.isFavorite,
                            isLoading = false,
                            hasUnsavedChanges = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = resourceProvider.getString(R.string.note_not_found),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = resourceProvider.getString(R.string.error_loading_note),
                    isLoading = false
                )
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(
            title = title,
            hasUnsavedChanges = true
        )
    }

    fun updateContent(content: String) {
        _uiState.value = _uiState.value.copy(
            content = content,
            hasUnsavedChanges = true
        )
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    fun updateNoteType(noteType: NoteType) {
        val currentState = _uiState.value

        try {
            val newListItems = when (noteType) {
                NoteType.TEXT -> {
                    currentState.listItems
                }
                NoteType.LIST -> {
                    if (currentState.noteType == NoteType.TEXT && currentState.listItems.isEmpty()) {
                        listOf(createNewListItem(0))
                    } else {
                        currentState.listItems
                    }
                }
            }

            _uiState.value = currentState.copy(
                noteType = noteType,
                listItems = newListItems,
                hasUnsavedChanges = true
            )

        } catch (e: Exception) {
            _uiState.value = currentState.copy(
                error = resourceProvider.getString(R.string.error_updating_note)
            )
        }
    }

    fun updateListItems(items: List<NoteListItem>) {
        _uiState.value = _uiState.value.copy(
            listItems = items,
            hasUnsavedChanges = true
        )
    }

    fun reorderListItems(fromIndex: Int, toIndex: Int) {
        val currentItems = _uiState.value.listItems.toMutableList()

        if (fromIndex < currentItems.size && toIndex < currentItems.size) {
            val item = currentItems.removeAt(fromIndex)
            currentItems.add(toIndex, item)

            // Actualizar orden
            val reorderedItems = currentItems.mapIndexed { index, item ->
                item.copy(order = index)
            }

            updateListItems(reorderedItems)
        }
    }

    fun addListItem() {
        val currentItems = _uiState.value.listItems
        val newItem = createNewListItem(currentItems.size)

        // Asegurar que el nuevo item tenga un ID único
        val itemWithUniqueId = newItem.copy(
            id = "item_${System.currentTimeMillis()}_${currentItems.size}"
        )

        updateListItems(currentItems + itemWithUniqueId)
    }

    fun updateListItem(index: Int, updatedItem: NoteListItem) {
        val currentItems = _uiState.value.listItems.toMutableList()
        if (index < currentItems.size) {
            currentItems[index] = updatedItem
            updateListItems(currentItems)
        }
    }

    fun deleteListItem(index: Int) {
        val currentItems = _uiState.value.listItems.toMutableList()
        if (index < currentItems.size) {
            currentItems.removeAt(index)
            // Reordenar después de eliminar
            val reorderedItems = currentItems.mapIndexed { newIndex, item ->
                item.copy(order = newIndex)
            }
            updateListItems(reorderedItems)
        }
    }

    fun toggleItemCompletion(index: Int) {
        val currentItems = _uiState.value.listItems
        if (index < currentItems.size) {
            val item = currentItems[index]
            val updatedItem = item.copy(isCompleted = !item.isCompleted)
            updateListItem(index, updatedItem)
        }
    }

    fun changeItemIndent(index: Int, newIndentLevel: Int) {
        val currentItems = _uiState.value.listItems
        if (index < currentItems.size) {
            val item = currentItems[index]
            val clampedIndent = newIndentLevel.coerceIn(0, Dimensions.MaxIndentLevel)
            val updatedItem = item.copy(indentLevel = clampedIndent)
            updateListItem(index, updatedItem)
        }
    }

    fun toggleFavorite() {
        _uiState.value = _uiState.value.copy(
            isFavorite = !_uiState.value.isFavorite,
            hasUnsavedChanges = true
        )
    }

    fun saveNote(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.isEmpty) {
            _uiState.value = state.copy(
                error = resourceProvider.getString(R.string.empty_note_cannot_save)
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = state.copy(isSaving = true, error = null)

                val userId = userPreferences.getCurrentUserId().first()
                if (userId == null) {
                    _uiState.value = state.copy(
                        error = resourceProvider.getString(R.string.user_not_found),
                        isSaving = false
                    )
                    return@launch
                }

                val now = Clock.System.now().toEpochMilliseconds()

                val note = if (state.noteId != null) {
                    // Actualizar nota existente
                    Note(
                        id = state.noteId,
                        userId = userId,
                        title = state.title.trim(),
                        content = state.content,
                        noteType = state.noteType,
                        listItems = state.listItems,
                        isFavorite = state.isFavorite,
                        isArchived = false,
                        createdAt = 0L,
                        updatedAt = now
                    )
                } else {
                    // Crear nueva nota
                    Note(
                        id = 0L,
                        userId = userId,
                        title = state.title.trim(),
                        content = state.content,
                        noteType = state.noteType,
                        listItems = state.listItems,
                        isFavorite = state.isFavorite,
                        isArchived = false,
                        createdAt = now,
                        updatedAt = now
                    )
                }

                if (state.noteId != null) {
                    updateNoteUseCase(note)
                } else {
                    createNoteUseCase(note)
                }

                _uiState.value = state.copy(
                    isSaving = false,
                    hasUnsavedChanges = false
                )

                onSuccess()

            } catch (e: Exception) {
                _uiState.value = state.copy(
                    error = resourceProvider.getString(R.string.error_saving_note),
                    isSaving = false
                )
            }
        }
    }

    private fun createNewListItem(order: Int): NoteListItem {
        return NoteListItem(
            id = "item_${System.currentTimeMillis()}_$order",
            text = "",
            isCompleted = false,
            indentLevel = 0,
            order = order
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}