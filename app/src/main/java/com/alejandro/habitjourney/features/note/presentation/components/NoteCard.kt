package com.alejandro.habitjourney.features.note.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.NoteType
import com.alejandro.habitjourney.core.presentation.ui.components.*
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import com.alejandro.habitjourney.features.note.domain.model.Note

@Composable
fun NoteCard(
    note: Note,
    onNoteClick: () -> Unit,
    onArchiveNote: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDeleteNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    HabitJourneyCard(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimensions.NoteCardMinHeight, max = Dimensions.NoteCardMaxHeight)
            .clickable { onNoteClick() }
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.SpacingMedium)
        ) {
            // Header con título y acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
                ) {
                    // Indicador de tipo
                    Icon(
                        imageVector = when (note.noteType) {
                            NoteType.TEXT -> Icons.Default.Description
                            NoteType.LIST -> Icons.Default.Checklist
                        },
                        contentDescription = null,
                        tint = AcentoInformativo,
                        modifier = Modifier.size(Dimensions.IconSizeSmall)
                    )

                    // Título
                    Text(
                        text = note.title.ifBlank { stringResource(R.string.note_untitled) },
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Indicador de favorito
                    if (note.isFavorite) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = stringResource(R.string.favorite_note),
                            tint = AcentoPositivo,
                            modifier = Modifier.size(Dimensions.IconSizeSmall)
                        )
                    }
                }

                // Menú de acciones
                Box {
                    IconButton(
                        onClick = { showMenu = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.more_options),
                            modifier = Modifier.size(Dimensions.IconSizeSmall)
                        )
                    }

                    NoteContextMenu(
                        expanded = showMenu,
                        onDismiss = { showMenu = false },
                        note = note,
                        onArchiveNote = {
                            onArchiveNote()
                            showMenu = false
                        },
                        onToggleFavorite = {
                            onToggleFavorite()
                            showMenu = false
                        },
                        onDeleteNote = {
                            onDeleteNote()
                            showMenu = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

            // Contenido preview
            when (note.noteType) {
                NoteType.TEXT -> {
                    if (note.content.isNotBlank()) {
                        Text(
                            text = note.preview,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = Dimensions.NotePreviewMaxLines,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                NoteType.LIST -> {
                    NoteListPreview(note = note)
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

            // Footer con estadísticas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Contador de palabras
                Text(
                    text = stringResource(R.string.note_word_count, note.wordCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Estado archivado
                if (note.isArchived) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = null,
                            modifier = Modifier.size(Dimensions.IconSizeSmall),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.archived),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}