package com.alejandro.habitjourney.features.note.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import java.util.*
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

    // Debounced word count calculation
    @OptIn(FlowPreview::class)
    private val wordCountFlow = combine(
        _uiState.map { it.title },
        _uiState.map { it.content },
        _uiState.map { it.listItems },
        _uiState.map { it.noteType }
    ) { title, content, listItems, noteType ->
        calculateWordCount(title, content, listItems, noteType)
    }.debounce(Dimensions.WordCountDebounceMs)

    init {
        // Update word count reactively
        viewModelScope.launch {
            wordCountFlow.collect { wordCount ->
                _uiState.value = _uiState.value.copy(wordCount = wordCount)
            }
        }
    }

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

        Log.d("CreateEditNoteVM", "Changing note type from ${currentState.noteType} to $noteType")

        try {
            val newListItems = when (noteType) {
                NoteType.TEXT -> {
                    Log.d("CreateEditNoteVM", "Switching to TEXT, clearing list items")
                    emptyList()
                }
                NoteType.LIST -> {
                    if (currentState.noteType == NoteType.TEXT && currentState.listItems.isEmpty()) {
                        Log.d("CreateEditNoteVM", "Switching to LIST, creating first empty item")
                        listOf(createNewListItem(0))
                    } else {
                        Log.d("CreateEditNoteVM", "Keeping existing list items")
                        currentState.listItems
                    }
                }
            }

            _uiState.value = currentState.copy(
                noteType = noteType,
                listItems = newListItems,
                hasUnsavedChanges = true
            )

            Log.d("CreateEditNoteVM", "Note type changed successfully")

        } catch (e: Exception) {
            Log.e("CreateEditNoteVM", "Error changing note type", e)
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
                        updatedAt = now,
                        wordCount = state.wordCount
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
                        updatedAt = now,
                        wordCount = state.wordCount
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

    // Auto-save para lista de items
    fun autoSaveIfNeeded() {
        val state = _uiState.value
        if (state.hasUnsavedChanges &&
            !state.isEmpty &&
            !state.isSaving &&
            state.noteId != null) {

            viewModelScope.launch {
                try {
                    val userId = userPreferences.getCurrentUserId().first()
                    if (userId != null) {
                        val note = Note(
                            id = state.noteId,
                            userId = userId,
                            title = state.title.trim(),
                            content = state.content,
                            noteType = state.noteType,
                            listItems = state.listItems,
                            isFavorite = state.isFavorite,
                            isArchived = false,
                            createdAt = 0L,
                            updatedAt = Clock.System.now().toEpochMilliseconds(),
                            wordCount = state.wordCount
                        )
                        updateNoteUseCase(note)
                        _uiState.value = state.copy(hasUnsavedChanges = false)
                    }
                } catch (e: Exception) {
                    // Silenciar errores de auto-guardado
                }
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

    private fun calculateWordCount(
        title: String,
        content: String,
        listItems: List<NoteListItem>,
        noteType: NoteType
    ): Int {
        return when (noteType) {
            NoteType.TEXT -> {
                val titleWords = if (title.isBlank()) 0 else title.split("\\s+".toRegex()).size
                val contentWords = if (content.isBlank()) 0 else content.split("\\s+".toRegex()).size
                titleWords + contentWords
            }
            NoteType.LIST -> {
                val titleWords = if (title.isBlank()) 0 else title.split("\\s+".toRegex()).size
                val listWords = listItems.sumOf { item ->
                    if (item.text.isBlank()) 0 else item.text.split("\\s+".toRegex()).size
                }
                titleWords + listWords
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}