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

        // Eliminar
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