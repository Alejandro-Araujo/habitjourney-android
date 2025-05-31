package com.alejandro.habitjourney.features.task.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.features.task.domain.model.Task


@Composable
fun TaskContextMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    task: Task,
    onArchiveTask: () -> Unit,
    modifier: Modifier = Modifier
) {
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
        }
    }
}