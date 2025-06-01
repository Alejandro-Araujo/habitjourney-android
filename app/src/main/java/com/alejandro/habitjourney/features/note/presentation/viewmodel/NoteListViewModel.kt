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

    // Estado combinado de la UI
    private val _baseUiState = combine(
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
    }

    val uiState: StateFlow<NoteListUiState> = combine(
        _baseUiState,
        _stats
    ) { baseState, stats ->
        baseState.copy(
            totalNotesCount = stats.activeNotesCount,
            totalWordCount = stats.totalWordCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(Dimensions.StateFlowTimeout),
        initialValue = NoteListUiState()
    )

    // Flow de notas basado en filtros
    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<Note>> = combine(
        _currentFilter,
        _searchQuery,
        userPreferences.getCurrentUserId()
    ) { filter, searchQuery, userId ->
        if (userId != null) {
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
        } else {
            flowOf(emptyList())
        }
    }.flatMapLatest { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(Dimensions.StateFlowTimeout),
            initialValue = emptyList()
        )

    init {
        loadStats()
    }

    fun setFilter(filter: NoteFilterType) {
        _currentFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch() {
        _isSearchActive.value = !_isSearchActive.value
        if (!_isSearchActive.value) {
            _searchQuery.value = ""
        }
    }

    fun archiveNote(noteId: Long, isArchived: Boolean = true) {
        viewModelScope.launch {
            try {
                archiveNoteUseCase(noteId, isArchived)
            } catch (e: Exception) {
                _error.value = resourceProvider.getString(R.string.error_archiving_note)
            }
        }
    }

    fun toggleFavorite(noteId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                toggleFavoriteNoteUseCase(noteId, isFavorite)
            } catch (e: Exception) {
                _error.value = resourceProvider.getString(R.string.error_updating_note)
            }
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            try {
                deleteNoteUseCase(noteId)
                loadStats()
            } catch (e: Exception) {
                _error.value = resourceProvider.getString(R.string.error_deleting_note)
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            try {
                val userId = userPreferences.getCurrentUserId().first()
                if (userId != null) {
                    val stats = getNoteStatsUseCase(userId)
                    _stats.value = stats
                }
            } catch (e: Exception) {
                // Silenciar errores de estadísticas
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun refreshNotes() {
        loadStats()
    }
}