package com.alejandro.habitjourney.features.task.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.features.task.domain.model.Task

/**
 * Un componente Composable que muestra un menú desplegable con acciones contextuales para una [Task].
 *
 * Este menú ofrece opciones para archivar/desarchivar y, opcionalmente, eliminar una tarea,
 * dependiendo de su estado actual y de los callbacks proporcionados.
 *
 * @param modifier El [Modifier] a aplicar a este composable.
 * @param expanded Indica si el menú desplegable está actualmente expandido y visible.
 * @param onDismiss Lambda que se invoca cuando el menú desplegable se cierra (por ejemplo, haciendo clic fuera).
 * @param task La [Task] para la cual se muestra el menú contextual.
 * @param onArchiveTask Lambda que se invoca cuando se hace clic en la opción "Archivar tarea".
 * @param onUnarchiveTask Lambda que se invoca cuando se hace clic en la opción "Desarchivar tarea". Por defecto, es una lambda vacía.
 * @param onDeleteTask Lambda opcional que se invoca cuando se hace clic en la opción "Eliminar". Si es `null`, la opción de eliminar no se muestra.
 */
@Composable
fun TaskContextMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onDismiss: () -> Unit,
    task: Task,
    onArchiveTask: () -> Unit,
    onUnarchiveTask: () -> Unit = {},
    onDeleteTask: (() -> Unit)? = null)
{
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        if (!task.isArchived) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.archive_task)) },
                onClick = onArchiveTask,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = null
                    )
                }
            )
        } else {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.unarchive_task)) },
                onClick = {
                    onUnarchiveTask()
                    onDismiss()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Unarchive,
                        contentDescription = null
                    )
                }
            )
        }

        HorizontalDivider()

        if (onDeleteTask != null) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.delete)) },
                onClick = {
                    onDeleteTask()
                    onDismiss()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null
                    )
                }
            )
        }
    }
}