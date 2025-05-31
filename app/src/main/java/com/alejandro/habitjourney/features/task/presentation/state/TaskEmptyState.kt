package com.alejandro.habitjourney.features.task.presentation.state


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButton
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButtonType
import com.alejandro.habitjourney.core.presentation.ui.theme.*

@Composable
fun TaskEmptyState(
    currentFilter: TaskFilterType,
    onCreateTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icono principal
        Icon(
            imageVector = when (currentFilter) {
                TaskFilterType.ACTIVE -> Icons.AutoMirrored.Filled.Assignment
                TaskFilterType.COMPLETED -> Icons.Default.CheckCircle
                TaskFilterType.ARCHIVED -> Icons.Default.Archive
                TaskFilterType.OVERDUE -> Icons.Default.Warning
                TaskFilterType.ALL -> Icons.Default.Task
            },
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        // Título principal
        Text(
            text = when (currentFilter) {
                TaskFilterType.ACTIVE -> stringResource(R.string.no_active_tasks)
                TaskFilterType.COMPLETED -> stringResource(R.string.no_completed_tasks)
                TaskFilterType.ARCHIVED -> stringResource(R.string.no_archived_tasks)
                TaskFilterType.OVERDUE -> stringResource(R.string.no_overdue_tasks)
                TaskFilterType.ALL -> stringResource(R.string.no_tasks)
            },
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

        Text(
            text = when (currentFilter) {
                TaskFilterType.ACTIVE -> stringResource(R.string.no_active_tasks_subtitle)
                TaskFilterType.COMPLETED -> stringResource(R.string.no_completed_tasks_subtitle)
                TaskFilterType.ARCHIVED -> stringResource(R.string.no_archived_tasks_subtitle)
                TaskFilterType.OVERDUE -> stringResource(R.string.no_overdue_tasks_subtitle)
                TaskFilterType.ALL -> stringResource(R.string.no_tasks_subtitle)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        // Botón CTA
        if (currentFilter == TaskFilterType.ACTIVE) {
            Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

            HabitJourneyButton(
                text = stringResource(R.string.create_first_task),
                onClick = onCreateTask,
                type = HabitJourneyButtonType.PRIMARY,
                leadingIcon = Icons.Default.Add,
                iconContentDescription = stringResource(R.string.add_task),
                modifier = Modifier.width(200.dp)
            )
        }
    }
}