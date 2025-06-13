package com.alejandro.habitjourney.features.note.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.NoteType
import com.alejandro.habitjourney.core.presentation.ui.components.*
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import com.alejandro.habitjourney.features.note.domain.model.Note
import com.alejandro.habitjourney.features.note.presentation.components.*
import com.alejandro.habitjourney.features.note.presentation.viewmodel.NoteDetailsViewModel
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailsScreen(
    noteId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: NoteDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val note by viewModel.note.collectAsStateWithLifecycle()

    // Inicializar el ViewModel
    LaunchedEffect(noteId) {
        viewModel.initializeWithNoteId(noteId)
    }

    // Estados para diálogos de confirmación
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }
    var showMenuDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoading, uiState.noteExists) {
        if (!uiState.isLoading && !uiState.noteExists && uiState.error != null) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.note_details),
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                },
                actions = {
                    if (note != null && !uiState.isLoading) {
                        // Botón de editar
                        IconButton(
                            onClick = { onNavigateToEdit(noteId) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit_note)
                            )
                        }

                        // Menú con más opciones
                        Box {
                            IconButton(
                                onClick = { showMenuDropdown = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = stringResource(R.string.more_options)
                                )
                            }

                            note?.let { currentNote ->
                                NoteContextMenu(
                                    expanded = showMenuDropdown,
                                    onDismiss = { showMenuDropdown = false },
                                    note = currentNote,
                                    onArchiveNote = {
                                        if (!currentNote.isArchived) {
                                            showArchiveDialog = true
                                        } else {
                                            viewModel.archiveNote(onNavigateBack)
                                        }
                                        showMenuDropdown = false
                                    },
                                    onUnarchiveNote = {
                                        if (currentNote.isArchived) {
                                            showArchiveDialog = true
                                        } else {
                                            viewModel.archiveNote(onNavigateBack)
                                        }
                                        showMenuDropdown = false
                                    },
                                    onToggleFavorite = {
                                        viewModel.toggleFavorite()
                                        showMenuDropdown = false
                                    },
                                    onDeleteNote = {
                                        showDeleteDialog = true
                                        showMenuDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            note?.let { currentNote ->
                NoteDetailsContent(
                    note = currentNote,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            // Overlay de carga
            if (uiState.isLoading) {
                HabitJourneyLoadingOverlay()
            }
        }
    }

    // Diálogos de confirmación
    if (showDeleteDialog) {
        ConfirmationDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = stringResource(R.string.delete_note_title),
            message = stringResource(R.string.delete_note_message),
            onConfirm = {
                viewModel.deleteNote(onNavigateBack)
                showDeleteDialog = false
            },
            confirmText = stringResource(R.string.delete),
            cancelText = stringResource(R.string.cancel),
            icon = Icons.Default.Warning
        )
    }

    if (showArchiveDialog) {
        note?.let { currentNote ->
            ConfirmationDialog(
                onDismissRequest = { showArchiveDialog = false },
                title = if (!currentNote.isArchived) {
                    stringResource(R.string.archive_note_title)
                } else {
                    stringResource(R.string.unarchive_note_title)
                },
                message = if (!currentNote.isArchived) {
                    stringResource(R.string.archive_note_message)
                } else {
                    stringResource(R.string.unarchive_note_message)
                },
                onConfirm = {
                    viewModel.archiveNote(onNavigateBack)
                    showArchiveDialog = false
                },
                confirmText = if (!currentNote.isArchived) {
                    stringResource(R.string.archive)
                } else {
                    stringResource(R.string.unarchive)
                },
                cancelText = stringResource(R.string.cancel),
                icon = Icons.Default.Archive
            )
        }
    }
}

@Composable
private fun NoteDetailsContent(
    note: Note,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(Dimensions.SpacingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingLarge)
    ) {
        // Sección principal - Título y contenido
        NoteHeaderSection(note = note)

        // Tipo de nota y estado favorito
        NoteTypeAndFavoriteSection(note = note)

        // Contenido según el tipo
        when (note.noteType) {
            NoteType.TEXT -> {
                if (note.content.isNotBlank()) {
                    NoteContentSection(content = note.content)
                }
            }
            NoteType.LIST -> {
                if (note.listItems.isNotEmpty()) {
                    NoteListContentSection(listItems = note.listItems)
                }
            }
        }

        // Información de fechas
        NoteDatesSection(note = note)

        // Estado de la nota
        NoteStatusSection(note = note)
    }
}

@Composable
private fun NoteHeaderSection(
    note: Note,
    modifier: Modifier = Modifier
) {
    HabitJourneyCard(modifier = modifier) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 28.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (note.isFavorite) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = stringResource(R.string.favorite_note),
                        tint = AcentoPositivo,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteTypeAndFavoriteSection(
    note: Note,
    modifier: Modifier = Modifier
) {
    HabitJourneyCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (note.noteType) {
                    NoteType.TEXT -> Icons.Default.Description
                    NoteType.LIST -> Icons.Default.List
                },
                contentDescription = null,
                modifier = Modifier.size(Dimensions.IconSizeNormal),
                tint = AcentoInformativo
            )
            Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
            Text(
                text = stringResource(R.string.note_type_label),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = when (note.noteType) {
                    NoteType.TEXT -> stringResource(R.string.note_type_text)
                    NoteType.LIST -> stringResource(R.string.note_type_list)
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun NoteContentSection(
    content: String,
    modifier: Modifier = Modifier
) {
    HabitJourneyCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.IconSizeNormal),
                tint = AcentoInformativo
            )
            Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
            Text(
                text = stringResource(R.string.content),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun NoteListContentSection(
    listItems: List<com.alejandro.habitjourney.features.note.domain.model.NoteListItem>,
    modifier: Modifier = Modifier
) {
    HabitJourneyCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.IconSizeNormal),
                tint = AcentoInformativo
            )
            Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
            Text(
                text = stringResource(R.string.list_items),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
        ) {
            listItems.sortedBy { it.order }.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = (item.indentLevel * 16).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (item.isCompleted) {
                            Icons.Default.CheckCircle
                        } else {
                            Icons.Default.RadioButtonUnchecked
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (item.isCompleted) {
                            AcentoPositivo
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (item.isCompleted) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteDatesSection(
    note: Note,
    modifier: Modifier = Modifier
) {
    HabitJourneyCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.IconSizeNormal),
                tint = AcentoInformativo
            )
            Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
            Text(
                text = stringResource(R.string.dates),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

        // Fecha de creación
        val createdDate = Instant.fromEpochMilliseconds(note.createdAt)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        DateInfoRow(
            label = stringResource(R.string.created_date),
            date = formatDateForDisplay(createdDate),
            isError = false
        )

        // Fecha de actualización
        val updatedDate = Instant.fromEpochMilliseconds(note.updatedAt)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        DateInfoRow(
            label = stringResource(R.string.updated_date),
            date = formatDateForDisplay(updatedDate),
            isError = false
        )
    }
}

@Composable
private fun DateInfoRow(
    label: String,
    date: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = date,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = if (isError) Error else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun NoteStatusSection(
    note: Note,
    modifier: Modifier = Modifier
) {
    HabitJourneyCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.IconSizeNormal),
                tint = AcentoInformativo
            )
            Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
            Text(
                text = stringResource(R.string.note_status),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

        StatusInfoRow(
            label = stringResource(R.string.status),
            value = when {
                note.isArchived -> stringResource(R.string.archived)
                else -> stringResource(R.string.active)
            },
            color = when {
                note.isArchived -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> AcentoInformativo
            }
        )

        StatusInfoRow(
            label = stringResource(R.string.favorite_note),
            value = if (note.isFavorite) {
                stringResource(R.string.yes)
            } else {
                stringResource(R.string.no)
            },
            color = if (note.isFavorite) {
                AcentoPositivo
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun StatusInfoRow(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = color
        )
    }
}

// Función helper para formatear fechas
private fun formatDateForDisplay(date: LocalDate): String {
    return "${date.dayOfMonth}/${date.monthNumber}/${date.year}"
}

// Diálogo de confirmación
@Composable
private fun ConfirmationDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    onConfirm: () -> Unit,
    confirmText: String,
    cancelText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    HabitJourneyDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        message = message,
        dialogType = HabitJourneyDialogType.CONFIRMATION,
        icon = icon,
        confirmButtonText = confirmText,
        dismissButtonText = cancelText,
        onConfirm = onConfirm
    )
}