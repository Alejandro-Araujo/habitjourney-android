package com.alejandro.habitjourney.features.note.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.exception.ErrorHandler
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.note.domain.model.Note
import com.alejandro.habitjourney.features.note.domain.usecase.*
import com.alejandro.habitjourney.features.note.presentation.state.NoteDetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para gestionar los detalles de una nota específica.
 *
 * Responsabilidades:
 * - Cargar y observar cambios en una nota específica
 * - Manejar operaciones como favorito, archivo y eliminación
 * - Gestionar estados de carga y errores
 * - Proporcionar mensajes de error localizados
 */
@HiltViewModel
class NoteDetailsViewModel @Inject constructor(
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val toggleFavoriteNoteUseCase: ToggleFavoriteNoteUseCase,
    private val archiveNoteUseCase: ArchiveNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val errorHandler: ErrorHandler,
    private val resourceProvider: ResourceProvider,
    application: Application
) : AndroidViewModel(application) {

    private val _noteId = MutableStateFlow<Long?>(null)

    private val _uiState = MutableStateFlow(NoteDetailsUiState())
    val uiState: StateFlow<NoteDetailsUiState> = _uiState.asStateFlow()

    /**
     * Observa los cambios en la nota actual.
     * Se actualiza automáticamente cuando cambia el noteId.
     */
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

    /**
     * Inicializa el ViewModel con un ID de nota específico.
     *
     * @param noteId ID de la nota a cargar. Debe ser mayor a 0.
     */
    fun initializeWithNoteId(noteId: Long) {
        // Validar noteId
        if (noteId <= 0) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                noteExists = false,
                error = resourceProvider.getString(R.string.error_invalid_note_id)
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
                    error = if (noteData == null) resourceProvider.getString(R.string.note_not_found) else null
                )
            }
        }
    }

    /**
     * Alterna el estado de favorito de la nota actual.
     * Muestra errores si la operación falla.
     */
    fun toggleFavorite() {
        val currentNote = note.value ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isProcessing = true)
                toggleFavoriteNoteUseCase(currentNote.id, !currentNote.isFavorite)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = resourceProvider.getString(R.string.error_updating_note_favorite, errorHandler.getErrorMessage(e)),
                    isProcessing = false
                )
            } finally {
                _uiState.value = _uiState.value.copy(isProcessing = false)
            }
        }
    }

    /**
     * Archiva o desarchivar la nota actual.
     *
     * @param onSuccess Callback que se ejecuta cuando la operación es exitosa
     */
    fun archiveNote(onSuccess: () -> Unit) {
        val currentNote = note.value ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isProcessing = true)
                archiveNoteUseCase(currentNote.id, !currentNote.isArchived)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = resourceProvider.getString(R.string.error_archiving_note, errorHandler.getErrorMessage(e)),
                    isProcessing = false
                )
            }
        }
    }

    /**
     * Elimina la nota actual de forma permanente.
     *
     * @param onSuccess Callback que se ejecuta cuando la eliminación es exitosa
     */
    fun deleteNote(onSuccess: () -> Unit) {
        val currentNote = note.value ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isProcessing = true)
                deleteNoteUseCase(currentNote.id)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = resourceProvider.getString(R.string.error_deleting_note, errorHandler.getErrorMessage(e)),
                    isProcessing = false
                )
            }
        }
    }

    /**
     * Limpia el mensaje de error actual.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}