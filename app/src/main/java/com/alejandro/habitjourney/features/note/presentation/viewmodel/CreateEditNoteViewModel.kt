package com.alejandro.habitjourney.features.note.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.NoteType
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
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

/**
 * ViewModel para la pantalla de creación y edición de notas.
 *
 * Gestiona el estado de la UI ([CreateEditNoteUiState]), la lógica del formulario,
 * la validación de datos y la comunicación con los casos de uso para persistir las notas.
 *
 * @property createNoteUseCase Caso de uso para crear una nueva nota.
 * @property updateNoteUseCase Caso de uso para actualizar una nota existente.
 * @property getNoteByIdUseCase Caso de uso para obtener los datos de una nota por su ID.
 * @property userPreferences Preferencias para obtener el ID del usuario actual.
 * @property resourceProvider Proveedor de recursos para acceder a strings localizados.
 */
@HiltViewModel
class CreateEditNoteViewModel @Inject constructor(
    private val createNoteUseCase: CreateNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val userPreferences: UserPreferences,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateEditNoteUiState())
    val uiState: StateFlow<CreateEditNoteUiState> = _uiState.asStateFlow()

    /**
     * Inicializa el estado del ViewModel para una nota nueva o existente.
     * @param noteId El ID de la nota a editar. Si es `null`, se prepara para crear una nota nueva.
     * @param isReadOnly `true` si la nota debe abrirse en modo de solo lectura.
     */
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

    /**
     * Carga los datos de una nota existente desde el repositorio.
     * @param noteId El ID de la nota a cargar.
     */
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
                            createdAt = note.createdAt, // Preservar fecha de creación
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

    /** Actualiza el título de la nota en el estado de la UI. */
    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(
            title = title,
            hasUnsavedChanges = true
        )
    }

    /** Actualiza el contenido de la nota de texto en el estado de la UI. */
    fun updateContent(content: String) {
        _uiState.value = _uiState.value.copy(
            content = content,
            hasUnsavedChanges = true
        )
    }

    /** Actualiza el tipo de nota. Si se cambia a LISTA, añade un ítem inicial si no hay ninguno. */
    fun updateNoteType(noteType: NoteType) {
        val currentState = _uiState.value
        val newListItems = if (noteType == NoteType.LIST && currentState.listItems.isEmpty()) {
            listOf(createNewListItem(0))
        } else {
            currentState.listItems
        }
        _uiState.value = currentState.copy(
            noteType = noteType,
            listItems = newListItems,
            hasUnsavedChanges = true
        )
    }

    /** Actualiza la lista completa de ítems de una nota de tipo lista. */
    fun updateListItems(items: List<NoteListItem>) {
        _uiState.value = _uiState.value.copy(
            listItems = items,
            hasUnsavedChanges = true
        )
    }

    /** Añade un nuevo ítem en blanco al final de la lista. */
    fun addListItem() {
        val currentItems = _uiState.value.listItems
        val newItem = createNewListItem(currentItems.size)
        updateListItems(currentItems + newItem)
    }

    /** Elimina un ítem de la lista por su índice. */
    fun deleteListItem(index: Int) {
        val currentItems = _uiState.value.listItems.toMutableList()
        if (index in currentItems.indices) {
            currentItems.removeAt(index)
            val reorderedItems = currentItems.mapIndexed { newIndex, item -> item.copy(order = newIndex) }
            updateListItems(reorderedItems)
        }
    }

    /** Cambia el estado de completado de un ítem de la lista. */
    fun toggleItemCompletion(index: Int) {
        val currentItems = _uiState.value.listItems.toMutableList()
        if (index in currentItems.indices) {
            val item = currentItems[index]
            currentItems[index] = item.copy(isCompleted = !item.isCompleted)
            updateListItems(currentItems)
        }
    }

    /** Cambia el estado de favorito de la nota. */
    fun toggleFavorite() {
        _uiState.value = _uiState.value.copy(
            isFavorite = !_uiState.value.isFavorite,
            hasUnsavedChanges = true
        )
    }

    /**
     * Guarda la nota actual, ya sea creando una nueva o actualizando una existente.
     * @param onSuccess Callback que se ejecuta tras un guardado exitoso.
     */
    fun saveNote(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.isEmpty) {
            _uiState.value = state.copy(error = resourceProvider.getString(R.string.empty_note_cannot_save))
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = state.copy(isSaving = true, error = null)

                val userId = userPreferences.userIdFlow.first()
                    ?: throw IllegalStateException(resourceProvider.getString(R.string.user_not_found))

                val now = Clock.System.now().toEpochMilliseconds()

                val noteToSave = Note(
                    id = state.noteId ?: 0L,
                    userId = userId,
                    title = state.title.trim(),
                    content = state.content.trim(),
                    noteType = state.noteType,
                    listItems = state.listItems,
                    isFavorite = state.isFavorite,
                    isArchived = false,
                    createdAt = if (state.noteId != null) state.createdAt else now,
                    updatedAt = now
                )

                if (state.noteId != null) {
                    updateNoteUseCase(noteToSave)
                } else {
                    createNoteUseCase(noteToSave)
                }

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    hasUnsavedChanges = false
                )
                onSuccess()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = resourceProvider.getString(R.string.error_saving_note),
                    isSaving = false
                )
            }
        }
    }

    /** Crea una nueva instancia de [NoteListItem] con un ID único. */
    private fun createNewListItem(order: Int): NoteListItem {
        return NoteListItem(
            text = "",
            isCompleted = false,
            indentLevel = 0,
            order = order
        )
    }

    /** Limpia el mensaje de error del estado de la UI. */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
