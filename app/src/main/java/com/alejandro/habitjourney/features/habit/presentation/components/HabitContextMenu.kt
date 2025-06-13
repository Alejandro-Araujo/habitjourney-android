package com.alejandro.habitjourney.features.habit.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.alejandro.habitjourney.R

@Composable
fun HabitContextMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    isArchived: Boolean,
    isSkippedToday: Boolean,
    isCompletedToday: Boolean,
    canToggleSkipped: Boolean,
    onArchiveHabit: () -> Unit,
    onUnarchiveHabit: () -> Unit,
    onMarkSkipped: () -> Unit,
    onUndoSkipped: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        if (!isArchived) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_archive_habit)) },
                onClick = onArchiveHabit,
                leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) }
            )
        } else {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_unarchive_habit)) },
                onClick = onUnarchiveHabit,
                leadingIcon = { Icon(Icons.Default.Unarchive, contentDescription = null) }
            )
        }

        if (!isCompletedToday && !isSkippedToday && canToggleSkipped) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_skip)) },
                onClick = onMarkSkipped,
                leadingIcon = { Icon(Icons.Default.EventBusy, contentDescription = null) }
            )
        } else if (isSkippedToday) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_undo_skip)) },
                onClick = onUndoSkipped,
                leadingIcon = { Icon(Icons.Default.Remove, contentDescription = null) }
            )
        }
    }
}