package com.alejandro.habitjourney.features.note.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.NoteType
import com.alejandro.habitjourney.core.presentation.ui.components.*
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import com.alejandro.habitjourney.features.note.presentation.components.*
import com.alejandro.habitjourney.features.note.presentation.viewmodel.CreateEditNoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditNoteScreen(
    noteId: Long?,
    isReadOnly: Boolean = false,
    onNavigateBack: () -> Unit,
    viewModel: CreateEditNoteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    // Inicializar nota
    LaunchedEffect(noteId, isReadOnly) {
        viewModel.initializeNote(noteId, isReadOnly)
    }

    // Auto-save periódico
    LaunchedEffect(uiState.hasUnsavedChanges) {
        if (uiState.hasUnsavedChanges && noteId != null) {
            kotlinx.coroutines.delay(Dimensions.AutoSaveDelayMs)
            viewModel.autoSaveIfNeeded()
        }
    }

    // Manejar botón back con cambios sin guardar
    BackHandler(enabled = uiState.hasUnsavedChanges && !uiState.isReadOnly) {
        showUnsavedChangesDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (noteId == null) {
                            stringResource(R.string.create_note)
                        } else {
                            stringResource(R.string.edit_note)
                        },
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.hasUnsavedChanges && !uiState.isReadOnly) {
                                showUnsavedChangesDialog = true
                            } else {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                },
                actions = {
                    if (!uiState.isReadOnly) {
                        IconButton(
                            onClick = { viewModel.toggleFavorite() }
                        ) {
                            Icon(
                                imageVector = if (uiState.isFavorite) {
                                    Icons.Default.Favorite
                                } else {
                                    Icons.Default.FavoriteBorder
                                },
                                contentDescription = if (uiState.isFavorite) {
                                    stringResource(R.string.unfavorite_note)
                                } else {
                                    stringResource(R.string.favorite_note)
                                },
                                tint = if (uiState.isFavorite) {
                                    AcentoPositivo
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(Dimensions.SpacingMedium),
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
            ) {
                // Selector de tipo de nota
                if (!uiState.isReadOnly) {
                    NoteTypeSelector(
                        selectedType = uiState.noteType,
                        onTypeSelected = viewModel::updateNoteType
                    )
                }

                // Campo título
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = viewModel::updateTitle,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.note_title_hint)) },
                    readOnly = uiState.isReadOnly,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AcentoInformativo,
                        focusedLabelColor = AcentoInformativo
                    )
                )

                // Editor de contenido
                when (uiState.noteType) {
                    NoteType.TEXT -> {
                        NoteTextEditor(
                            content = uiState.content,
                            onContentChange = viewModel::updateContent,
                            isReadOnly = uiState.isReadOnly,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    NoteType.LIST -> {
                        NoteListEditor(
                            items = uiState.listItems,
                            onItemsChanged = viewModel::updateListItems,
                            onReorderItems = viewModel::reorderListItems,
                            onToggleItem = viewModel::toggleItemCompletion,
                            onDeleteItem = viewModel::deleteListItem,
                            onIndentChange = viewModel::changeItemIndent,
                            onAddItem = viewModel::addListItem,
                            isReadOnly = uiState.isReadOnly,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Información de la nota
                NoteInfoCard(
                    wordCount = uiState.wordCount,
                    hasUnsavedChanges = uiState.hasUnsavedChanges,
                    isAutoSaved = noteId != null && !uiState.hasUnsavedChanges
                )

                // BOTÓN DE GUARDAR
                if (!uiState.isReadOnly && uiState.canSave) {
                    HabitJourneyButton(
                        text = if (noteId == null) {
                            stringResource(R.string.create_note)
                        } else {
                            stringResource(R.string.save_note)
                        },
                        onClick = {
                            viewModel.saveNote {
                                keyboardController?.hide()
                                onNavigateBack()
                            }
                        },
                        type = HabitJourneyButtonType.PRIMARY,
                        enabled = uiState.canSave,
                        isLoading = uiState.isSaving,
                        leadingIcon = Icons.Default.Save,
                        iconContentDescription = stringResource(R.string.save_note),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Espaciado extra al final
                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
            }

            // Overlay de carga
            if (uiState.isLoading) {
                HabitJourneyLoadingOverlay()
            }
        }
    }

    if (showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.unsaved_changes),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.unsaved_changes_dialog_message),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnsavedChangesDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Error
                    )
                ) {
                    Text(stringResource(R.string.discard))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUnsavedChangesDialog = false }
                ) {
                    Text(stringResource(R.string.keep_editing))
                }
            },
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }

    // Mostrar errores
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }
}