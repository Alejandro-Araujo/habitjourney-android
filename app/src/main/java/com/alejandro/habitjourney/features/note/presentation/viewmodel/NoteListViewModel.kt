package com.alejandro.habitjourney.features.note.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.note.domain.model.Note
import com.alejandro.habitjourney.features.note.domain.model.NoteStats
import com.alejandro.habitjourney.features.note.domain.usecase.*
import com.alejandro.habitjourney.features.note.presentation.state.NoteFilterType
import com.alejandro.habitjourney.features.note.presentation.state.NoteListUiState
import com.alejandro.habitjourney.features.user.data.local.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla que muestra la lista de notas.
 *
 * Gestiona el estado de la UI ([NoteListUiState]), la carga de notas,
 * el filtrado y la búsqueda, y maneja las acciones del usuario como archivar,
 * marcar como favorito y eliminar notas.
 *
 * @property getAllNotesUseCase Caso de uso para obtener todas las notas.
 * @property getActiveNotesUseCase Caso de uso para obtener las notas activas.
 * @property getArchivedNotesUseCase Caso de uso para obtener las notas archivadas.
 * @property getFavoriteNotesUseCase Caso de uso para obtener las notas favoritas.
 * @property searchNotesUseCase Caso de uso para buscar notas.
 * @property archiveNoteUseCase Caso de uso para archivar o desarchivar una nota.
 * @property toggleFavoriteNoteUseCase Caso de uso para cambiar el estado de favorito.
 * @property deleteNoteUseCase Caso de uso para eliminar una nota.
 * @property getNoteStatsUseCase Caso de uso para obtener estadísticas de las notas.
 * @property userPreferences Preferencias para obtener el ID del usuario actual.
 * @property resourceProvider Proveedor de recursos para acceder a strings localizados.
 */
@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val getAllNotesUseCase: GetAllNotesUseCase,
    private val getActiveNotesUseCase: GetActiveNotesUseCase,
    private val getArchivedNotesUseCase: GetArchivedNotesUseCase,
    private val getFavoriteNotesUseCase: GetFavoriteNotesUseCase,
    private val searchNotesUseCase: SearchNotesUseCase,
    private val archiveNoteUseCase: ArchiveNoteUseCase,
    private val toggleFavoriteNoteUseCase: ToggleFavoriteNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val getNoteStatsUseCase: GetNoteStatsUseCase,
    private val userPreferences: UserPreferences,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _currentFilter = MutableStateFlow(NoteFilterType.ACTIVE)
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _isSearchActive = MutableStateFlow(false)
    private val _stats = MutableStateFlow(NoteStats(0, 0))

    /**
     * El estado de la UI que la vista observa.
     * Combina varios flujos de estado internos en un único objeto [NoteListUiState].
     */
    val uiState: StateFlow<NoteListUiState> = combine(
        _currentFilter,
        _searchQuery,
        _isLoading,
        _error,
        _isSearchActive
    ) { filter, search, loading, error, searchActive ->
        NoteListUiState(
            currentFilter = filter,
            searchQuery = search,
            isLoading = loading,
            error = error,
            isSearchActive = searchActive
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(Dimensions.StateFlowTimeout),
        initialValue = NoteListUiState()
    )

    /**
     * Flujo reactivo que emite la lista de notas filtrada y buscada.
     * Reacciona a los cambios en el filtro, la búsqueda y el ID de usuario.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<Note>> = combine(
        _currentFilter,
        _searchQuery,
        userPreferences.userIdFlow
    ) { filter, searchQuery, userId ->
        // Este bloque se ejecuta cada vez que el filtro, la búsqueda o el usuario cambian.
        if (userId == null) return@combine flowOf(emptyList())

        if (searchQuery.isNotBlank()) {
            searchNotesUseCase(userId, searchQuery)
        } else {
            when (filter) {
                NoteFilterType.ALL -> getAllNotesUseCase(userId)
                NoteFilterType.ACTIVE -> getActiveNotesUseCase(userId)
                NoteFilterType.ARCHIVED -> getArchivedNotesUseCase(userId)
                NoteFilterType.FAVORITES -> getFavoriteNotesUseCase(userId)
            }
        }
    }.flatMapLatest { it } // flatMapLatest cancela el flujo anterior y se suscribe al nuevo.
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(Dimensions.StateFlowTimeout),
            initialValue = emptyList()
        )

    init {
        loadStats()
    }

    /** Establece el filtro actual para la lista de notas. */
    fun setFilter(filter: NoteFilterType) {
        _currentFilter.value = filter
    }

    /** Actualiza el término de búsqueda. */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /** Activa o desactiva el modo de búsqueda. */
    fun toggleSearch() {
        _isSearchActive.value = !_isSearchActive.value
        if (!_isSearchActive.value) {
            _searchQuery.value = ""
        }
    }

    /** Archiva o desarchiva una nota. */
    fun archiveNote(noteId: Long, isArchived: Boolean = true) {
        viewModelScope.launch {
            try {
                archiveNoteUseCase(noteId, isArchived)
            } catch (e: Exception) {
                _error.value = resourceProvider.getString(R.string.error_archiving_note)
            }
        }
    }

    /** Cambia el estado de favorito de una nota. */
    fun toggleFavorite(noteId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                toggleFavoriteNoteUseCase(noteId, isFavorite)
            } catch (e: Exception) {
                _error.value = resourceProvider.getString(R.string.error_updating_note)
            }
        }
    }

    /** Elimina una nota permanentemente. */
    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            try {
                deleteNoteUseCase(noteId)
                loadStats() // Recarga las estadísticas después de eliminar.
            } catch (e: Exception) {
                _error.value = resourceProvider.getString(R.string.error_deleting_note)
            }
        }
    }

    /** Carga las estadísticas de las notas del usuario. */
    private fun loadStats() {
        viewModelScope.launch {
            try {
                userPreferences.userIdFlow.firstOrNull()?.let { userId ->
                    val stats = getNoteStatsUseCase(userId)
                    _stats.value = stats
                }
            } catch (e: Exception) {
                _error.value = resourceProvider.getString(R.string.error_loading_note)
            }
        }
    }

    /** Limpia el mensaje de error del estado de la UI. */
    fun clearError() {
        _error.value = null
    }
}
