package com.alejandro.habitjourney.features.note.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.theme.ErrorColor
import com.alejandro.habitjourney.features.note.domain.model.Note

/**
 * Menú contextual.
 *
 * Muestra opciones relevantes como marcar/desmarcar favorito, archivar/desarchivar y eliminar.
 *
 * @param expanded `true` si el menú debe estar visible.
 * @param onDismiss Callback para cuando se solicita cerrar el menú.
 * @param note El objeto [Note] para determinar qué acciones mostrar (ej: favorito vs. no favorito).
 * @param onArchiveNote Callback para la acción de archivar.
 * @param onUnarchiveNote Callback para la acción de desarchivar.
 * @param onToggleFavorite Callback para cambiar el estado de favorito.
 * @param onDeleteNote Callback para la acción de eliminar.
 */
@Composable
fun NoteContextMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    note: Note,
    onArchiveNote: () -> Unit,
    onUnarchiveNote: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDeleteNote: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        // Favorito/No favorito
        DropdownMenuItem(
            text = {
                Text(
                    if (note.isFavorite) {
                        stringResource(R.string.unfavorite_note)
                    } else {
                        stringResource(R.string.favorite_note)
                    }
                )
            },
            onClick = onToggleFavorite,
            leadingIcon = {
                Icon(
                    imageVector = if (note.isFavorite) {
                        Icons.Default.FavoriteBorder
                    } else {
                        Icons.Default.Favorite
                    },
                    contentDescription = null
                )
            }
        )

        // Archivar/Desarchivar
        DropdownMenuItem(
            text = {
                Text(
                    if (note.isArchived) stringResource(R.string.unarchive_note)
                    else stringResource(R.string.archive_note)
                )
            },
            onClick = {
                if (note.isArchived) {
                    onUnarchiveNote()
                } else {
                    onArchiveNote()
                }
            },
            leadingIcon = {
                Icon(
                    imageVector = if (note.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                    contentDescription = null
                )
            }
        )

        HorizontalDivider()

        // Eliminar
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(R.string.delete_note),
                    color = ErrorColor
                )
            },
            onClick = onDeleteNote,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = ErrorColor
                )
            }
        )
    }
}

