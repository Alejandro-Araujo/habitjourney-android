package com.alejandro.habitjourney.features.note.presentation.components

import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.unit.times
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.NoteType
import com.alejandro.habitjourney.core.presentation.ui.components.*
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import com.alejandro.habitjourney.features.note.domain.model.Note
import com.alejandro.habitjourney.features.note.domain.model.NoteListItem

/**
 * Componente de tarjeta reutilizable para mostrar una nota individual.
 *
 * Muestra el título, tipo, contenido (texto o lista) y estado (favorito, archivado) de una nota.
 * Gestiona las interacciones del usuario, como clics y menús contextuales, para
 * ejecutar acciones como archivar, eliminar o marcar como favorito.
 *
 * @param note El objeto [Note] que contiene todos los datos a mostrar.
 * @param onNoteClick Callback invocado con un clic normal, usualmente para navegar a la pantalla de edición.
 * @param onArchiveNote Callback para la acción de archivar la nota.
 * @param onUnarchiveNote Callback para la acción de desarchivar la nota.
 * @param onToggleFavorite Callback para cambiar el estado de favorito de la nota.
 * @param onDeleteNote Callback para la acción de eliminar permanentemente la nota.
 * @param modifier Modificador para personalizar el layout de la tarjeta.
 */
@Composable
fun NoteCard(
    note: Note,
    onNoteClick: () -> Unit,
    onArchiveNote: () -> Unit,
    onUnarchiveNote: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDeleteNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }

    HabitJourneyCard(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onNoteClick,
                onLongClick = { showMenu = true }
            ),
        containerColor = if (note.isArchived) {
            InactivoDeshabilitado.copy(alpha = AlphaValues.DisabledAlpha)
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor = MaterialTheme.colorScheme.onSurface,
        cardType = HabitJourneyCardType.ELEVATED,
        elevation = Dimensions.ElevationLevel2
    ) {
        Column {
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
                        style = Typography.headlineSmall.copy(
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
                            showArchiveDialog = true
                            showMenu = false
                        },
                        onUnarchiveNote = {
                            showArchiveDialog = true
                            showMenu = false
                        },
                        onToggleFavorite = {
                            onToggleFavorite()
                            showMenu = false
                        },
                        onDeleteNote = {
                            showDeleteDialog = true
                            showMenu = false
                        }
                    )
                }
            }

            // Contenido completo
            when (note.noteType) {
                NoteType.TEXT -> {
                    if (note.content.isNotBlank()) {
                        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
                        Text(
                            text = note.content,
                            style = Typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                NoteType.LIST -> {
                    if (note.listItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
                        NoteListComplete(items = note.listItems)
                    }
                }
            }

            // Diálogo para Archivar / Desarchivar
            if (showArchiveDialog) {
                ConfirmationDialog(
                    onDismissRequest = { showArchiveDialog = false },
                    title = if (!note.isArchived) stringResource(R.string.archive_note_title) else stringResource(R.string.unarchive_note_title),
                    message = if (!note.isArchived) stringResource(R.string.archive_note_message) else stringResource(R.string.unarchive_note_message),
                    onConfirm = {
                        if (note.isArchived) onUnarchiveNote() else onArchiveNote()
                        showArchiveDialog = false
                    },
                    icon = Icons.Default.Archive
                )
            }

            // Diálogo para Borrar
            if (showDeleteDialog) {
                ConfirmationDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = stringResource(R.string.delete_note_title),
                    message = stringResource(R.string.delete_note_message),
                    onConfirm = {
                        onDeleteNote()
                        showDeleteDialog = false
                    },
                    icon = Icons.Default.Warning
                )
            }

            // Footer con estado archivado (si aplica)
            if (note.isArchived) {
                Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = null,
                            modifier = Modifier.size(Dimensions.IconSizeSmall),
                            tint = InactivoDeshabilitado
                        )
                        Text(
                            text = stringResource(R.string.archived),
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Componente privado que renderiza la lista completa de ítems de una nota de tipo CHECKLIST.
 *
 * @param items La lista de [NoteListItem] a mostrar.
 * @param modifier Modificador para personalizar el layout de la columna.
 */
@Composable
private fun NoteListComplete(
    items: List<NoteListItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingExtraSmall)
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = (item.indentLevel * Dimensions.ListItemIndentSize)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
            ) {
                Icon(
                    imageVector = if (item.isCompleted) {
                        Icons.Default.CheckBox
                    } else {
                        Icons.Default.CheckBoxOutlineBlank
                    },
                    contentDescription = null,
                    tint = if (item.isCompleted) {
                        AcentoPositivo
                    } else {
                        InactivoDeshabilitado
                    },
                    modifier = Modifier.size(Dimensions.IconSizeSmall)
                )

                Text(
                    text = item.text,
                    style = Typography.bodyMedium,
                    color = if (item.isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AlphaValues.MediumAlpha)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
