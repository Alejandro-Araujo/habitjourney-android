package com.alejandro.habitjourney.features.note.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.features.note.domain.model.Note
import com.alejandro.habitjourney.features.note.domain.usecase.*
import com.alejandro.habitjourney.features.note.presentation.state.NoteDetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailsViewModel @Inject constructor(
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val toggleFavoriteNoteUseCase: ToggleFavoriteNoteUseCase,
    private val archiveNoteUseCase: ArchiveNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModel() {

    private val _noteId = MutableStateFlow<Long?>(null)

    private val _uiState = MutableStateFlow(NoteDetailsUiState())
    val uiState: StateFlow<NoteDetailsUiState> = _uiState.asStateFlow()

    // Observar la nota
    @OptIn(ExperimentalCoroutinesApi::class)
    val note: StateFlow<Note?> = _noteId
        .filterNotNull()
        .flatMapLatest { noteId ->
            getNoteByIdUseCase(noteId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun initializeWithNoteId(noteId: Long) {
        // Validar noteId
        if (noteId <= 0) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                noteExists = false,
                error = "ID de nota inválido"
            )
            return
        }

        _noteId.value = noteId
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            noteExists = false,
            error = null
        )

        // Observar cambios en la nota
        viewModelScope.launch {
            note.collect { noteData ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    noteExists = noteData != null,
                    error = if (noteData == null) "Nota no encontrada" else null
                )
            }
        }
    }

    fun toggleFavorite() {
        val currentNote = note.value ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isProcessing = true)
                toggleFavoriteNoteUseCase(currentNote.id, !currentNote.isFavorite)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al actualizar la nota",
                    isProcessing = false
                )
            } finally {
                _uiState.value = _uiState.value.copy(isProcessing = false)
            }
        }
    }

    fun archiveNote(onSuccess: () -> Unit) {
        val currentNote = note.value ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isProcessing = true)
                archiveNoteUseCase(currentNote.id, !currentNote.isArchived)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al archivar la nota",
                    isProcessing = false
                )
            }
        }
    }

    fun deleteNote(onSuccess: () -> Unit) {
        val currentNote = note.value ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isProcessing = true)
                deleteNoteUseCase(currentNote.id)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al eliminar la nota",
                    isProcessing = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}