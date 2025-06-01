package com.alejandro.habitjourney.features.note.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.*
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import com.alejandro.habitjourney.features.note.presentation.components.*
import com.alejandro.habitjourney.features.note.presentation.viewmodel.NoteListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    onNavigateToCreateNote: () -> Unit,
    onNavigateToEditNote: (Long) -> Unit,
    viewModel: NoteListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Manejo de errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.notes),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
                    actions = {
                        // Botón de búsqueda
                        IconButton(
                            onClick = { viewModel.toggleSearch() }
                        ) {
                            Icon(
                                imageVector = if (uiState.isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (uiState.isSearchActive) {
                                    stringResource(R.string.close_search)
                                } else {
                                    stringResource(R.string.search_notes)
                                }
                            )
                        }
                    }
                )

                // Barra de búsqueda
                if (uiState.isSearchActive) {
                    NotesSearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::setSearchQuery,
                        onClose = { viewModel.toggleSearch() },
                        modifier = Modifier.padding(horizontal = Dimensions.SpacingMedium)
                    )
                }
            }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Filtros
                NotesFilterDropdown(
                    currentFilter = uiState.currentFilter,
                    onFilterSelected = viewModel::setFilter,
                    modifier = Modifier.padding(horizontal = Dimensions.SpacingMedium)
                )

                // Estadísticas (si hay notas)
                if (notes.isNotEmpty()) {
                    NoteStatsCard(
                        totalNotes = uiState.totalNotesCount,
                        totalWords = uiState.totalWordCount,
                        modifier = Modifier.padding(
                            horizontal = Dimensions.SpacingMedium,
                            vertical = Dimensions.SpacingSmall
                        )
                    )
                }

                Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

                // Lista de notas
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
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
                            onNoteClick = { onNavigateToEditNote(note.id) },
                            onArchiveNote = {
                                viewModel.archiveNote(note.id, !note.isArchived)
                            },
                            onToggleFavorite = {
                                viewModel.toggleFavorite(note.id, !note.isFavorite)
                            },
                            onDeleteNote = {
                                viewModel.deleteNote(note.id)
                            }
                        )
                    }

                    // Estado vacío
                    if (notes.isEmpty() && !uiState.isLoading) {
                        item {
                            NoteEmptyState(
                                currentFilter = uiState.currentFilter,
                                searchQuery = uiState.searchQuery,
                                onCreateNote = onNavigateToCreateNote,
                                modifier = Modifier.fillParentMaxSize()
                            )
                        }
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
