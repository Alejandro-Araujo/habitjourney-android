package com.alejandro.habitjourney.features.task.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.FilterOption
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyEmptyState
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyFloatingActionButton
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyLoadingOverlay
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneySearchableTopBar
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import com.alejandro.habitjourney.features.task.presentation.components.TaskCard
import com.alejandro.habitjourney.features.task.presentation.state.TaskFilterType
import com.alejandro.habitjourney.features.task.presentation.viewmodel.TaskListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onNavigateToCreateTask: () -> Unit,
    onNavigateToEditTask: (Long) -> Unit,
    onNavigateToTaskDetail: (Long) -> Unit,
    viewModel: TaskListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val filterOptions = TaskFilterType.entries.map { filterType ->
        FilterOption(
            value = filterType,
            label = when (filterType) {
                TaskFilterType.ALL -> stringResource(R.string.filter_all)
                TaskFilterType.ACTIVE -> stringResource(R.string.filter_active)
                TaskFilterType.COMPLETED -> stringResource(R.string.filter_completed)
                TaskFilterType.ARCHIVED -> stringResource(R.string.filter_archived)
                TaskFilterType.OVERDUE -> stringResource(R.string.filter_overdue)
            },
            icon = when (filterType) {
                TaskFilterType.ALL -> Icons.AutoMirrored.Filled.List
                TaskFilterType.ACTIVE -> Icons.Default.Schedule
                TaskFilterType.COMPLETED -> Icons.Default.CheckCircle
                TaskFilterType.ARCHIVED -> Icons.Default.Archive
                TaskFilterType.OVERDUE -> Icons.Default.Warning
            }
        )
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            HabitJourneySearchableTopBar(
                title = stringResource(R.string.tasks),
                isSearchActive = uiState.isSearchActive,
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::setSearchQuery,
                onSearchToggle = viewModel::toggleSearch,
                currentFilter = uiState.currentFilter,
                filterOptions = filterOptions,
                onFilterSelected = viewModel::setFilter,
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            HabitJourneyFloatingActionButton(
                onClick = onNavigateToCreateTask,
                icon = Icons.Default.Add,
                containerColor = AcentoInformativo,
                iconContentDescription = stringResource(R.string.add_task)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Dimensions.SpacingMedium,
                        end = Dimensions.SpacingMedium,
                        bottom = Dimensions.FabBottomPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
                ) {
                    items(
                        items = tasks,
                        key = { it.id }
                    ) { task ->
                        TaskCard(
                            task = task,
                            onTaskClick = { onNavigateToTaskDetail(task.id) },
                            onTaskLongClick = { /* Lógica futura */ },
                            onToggleCompletion = { isCompleted ->
                                viewModel.toggleTaskCompletion(task.id, isCompleted)
                            },
                            onArchiveTask = {
                                viewModel.archiveTask(task.id)
                            },
                            onUnarchiveTask = {
                                viewModel.unarchiveTask(task.id)
                            },
                            onDeleteTask = {
                                viewModel.deleteTask(task.id)
                            }
                        )
                    }

                    if (tasks.isEmpty() && !uiState.isLoading) {
                        item {
                            HabitJourneyEmptyState(
                                modifier = Modifier.fillParentMaxSize(),
                                icon = getTaskEmptyStateIcon(uiState.currentFilter),
                                title = getTaskEmptyStateTitle(uiState.currentFilter, uiState.searchQuery),
                                description = getTaskEmptyStateMessage(uiState.currentFilter),
                                actionButtonText = if (uiState.currentFilter == TaskFilterType.ACTIVE && uiState.searchQuery.isBlank()) {
                                    stringResource(R.string.create_first_task)
                                } else null,
                                onActionClick = if (uiState.currentFilter == TaskFilterType.ACTIVE && uiState.searchQuery.isBlank()) {
                                    onNavigateToCreateTask
                                } else null
                            )
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                HabitJourneyLoadingOverlay()
            }
        }
    }
}

@Composable
private fun getTaskEmptyStateTitle(filter: TaskFilterType, searchQuery: String): String {
    return if (searchQuery.isNotBlank()) {
        stringResource(R.string.no_tasks)
    } else {
        when (filter) {
            TaskFilterType.ACTIVE -> stringResource(R.string.no_active_tasks)
            TaskFilterType.COMPLETED -> stringResource(R.string.no_completed_tasks)
            TaskFilterType.ARCHIVED -> stringResource(R.string.no_archived_tasks)
            TaskFilterType.OVERDUE -> stringResource(R.string.no_overdue_tasks)
            TaskFilterType.ALL -> stringResource(R.string.no_tasks)
        }
    }
}

@Composable
private fun getTaskEmptyStateMessage(filter: TaskFilterType): String {
    return when (filter) {
        TaskFilterType.ACTIVE -> stringResource(R.string.no_active_tasks_subtitle)
        TaskFilterType.COMPLETED -> stringResource(R.string.no_completed_tasks_subtitle)
        TaskFilterType.ARCHIVED -> stringResource(R.string.no_archived_tasks_subtitle)
        TaskFilterType.OVERDUE -> stringResource(R.string.no_overdue_tasks_subtitle)
        TaskFilterType.ALL -> stringResource(R.string.no_tasks_subtitle)
    }
}

@Composable
private fun getTaskEmptyStateIcon(filter: TaskFilterType): ImageVector {
    return when (filter) {
        TaskFilterType.ACTIVE -> Icons.AutoMirrored.Filled.Assignment
        TaskFilterType.COMPLETED -> Icons.Default.CheckCircle
        TaskFilterType.ARCHIVED -> Icons.Default.Archive
        TaskFilterType.OVERDUE -> Icons.Default.Warning
        TaskFilterType.ALL -> Icons.Default.Task
    }
}