package com.alejandro.habitjourney.features.note.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.*
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import com.alejandro.habitjourney.features.note.presentation.components.*
import com.alejandro.habitjourney.features.note.presentation.state.NoteFilterType
import com.alejandro.habitjourney.features.note.presentation.viewmodel.NoteListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    onNavigateToCreateNote: () -> Unit,
    onNavigateToEditNote: (Long) -> Unit,
    onNavigateToNoteDetail: (Long) -> Unit,
    viewModel: NoteListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            HabitJourneySearchableTopBar(
                title = stringResource(R.string.notes),
                isSearchActive = uiState.isSearchActive,
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::setSearchQuery,
                onSearchToggle = viewModel::toggleSearch,
                currentFilter = uiState.currentFilter,
                filterOptions = listOf(
                    FilterOption(
                        value = NoteFilterType.ALL,
                        label = stringResource(R.string.filter_notes_all),
                        icon = Icons.AutoMirrored.Filled.List
                    ),
                    FilterOption(
                        value = NoteFilterType.ACTIVE,
                        label = stringResource(R.string.filter_notes_active),
                        icon = Icons.Default.Description
                    ),
                    FilterOption(
                        value = NoteFilterType.FAVORITES,
                        label = stringResource(R.string.filter_notes_favorites),
                        icon = Icons.Default.Favorite
                    ),
                    FilterOption(
                        value = NoteFilterType.ARCHIVED,
                        label = stringResource(R.string.filter_notes_archived),
                        icon = Icons.Default.Archive
                    )
                ),
                onFilterSelected = viewModel::setFilter,
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            HabitJourneyFloatingActionButton(
                onClick = onNavigateToCreateNote,
                icon = Icons.Default.Add,
                containerColor = AcentoInformativo,
                iconContentDescription = stringResource(R.string.add_note)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (notes.isEmpty() && !uiState.isLoading) {
                // Estado vacío
                HabitJourneyEmptyState(
                    icon = Icons.Default.Note,
                    title = getEmptyStateTitle(uiState.currentFilter, uiState.searchQuery),
                    description = getEmptyStateMessage(uiState.currentFilter, uiState.searchQuery),
                    actionButtonText = if (uiState.searchQuery.isBlank() && uiState.currentFilter != NoteFilterType.ARCHIVED) {
                        stringResource(R.string.create_first_note)
                    } else null,
                    onActionClick = if (uiState.searchQuery.isBlank() && uiState.currentFilter != NoteFilterType.ARCHIVED) {
                        onNavigateToCreateNote
                    } else null
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(
                        top = Dimensions.SpacingMedium,
                        start = Dimensions.SpacingMedium,
                        end = Dimensions.SpacingMedium,
                        bottom = Dimensions.FabBottomPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
                ) {
                    items(
                        items = notes,
                        key = { it.id }
                    ) { note ->
                        NoteCard(
                            note = note,
                            onNoteClick = { onNavigateToNoteDetail (note.id) },
                            onArchiveNote = {
                                viewModel.archiveNote(note.id, !note.isArchived)
                            },
                            onUnarchiveNote = { viewModel.archiveNote(note.id, false) },
                            onToggleFavorite = {
                                viewModel.toggleFavorite(note.id, !note.isFavorite)
                            },
                            onDeleteNote = {
                                viewModel.deleteNote(note.id)
                            }
                        )
                    }
                }
            }

            // Overlay de carga
            if (uiState.isLoading) {
                HabitJourneyLoadingOverlay()
            }
        }
    }
}

@Composable
private fun getEmptyStateTitle(filter: NoteFilterType, searchQuery: String): String {
    return if (searchQuery.isNotBlank()) {
        stringResource(R.string.no_notes_found_search)
    } else {
        when (filter) {
            NoteFilterType.ALL -> stringResource(R.string.no_notes_all_title)
            NoteFilterType.ACTIVE -> stringResource(R.string.no_notes_active_title)
            NoteFilterType.ARCHIVED -> stringResource(R.string.no_notes_archived_title)
            NoteFilterType.FAVORITES -> stringResource(R.string.no_notes_favorites_title)
        }
    }
}

@Composable
private fun getEmptyStateMessage(filter: NoteFilterType, searchQuery: String): String {
    return if (searchQuery.isNotBlank()) {
        stringResource(R.string.no_notes_found_search_message, searchQuery)
    } else {
        when (filter) {
            NoteFilterType.ALL -> stringResource(R.string.no_notes_all_message)
            NoteFilterType.ACTIVE -> stringResource(R.string.no_notes_active_message)
            NoteFilterType.ARCHIVED -> stringResource(R.string.no_notes_archived_message)
            NoteFilterType.FAVORITES -> stringResource(R.string.no_notes_favorites_message)
        }
    }
}