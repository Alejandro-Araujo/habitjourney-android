package com.alejandro.habitjourney.features.task.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyFloatingActionButton
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyLoadingOverlay
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import com.alejandro.habitjourney.features.task.presentation.components.TaskCard
import com.alejandro.habitjourney.features.task.presentation.components.TaskEmptyState
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
    val keyboardController = LocalSoftwareKeyboardController.current

    // Manejo de errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.tasks),
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    // Botón de búsqueda
                    IconButton(onClick = { viewModel.toggleSearch() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = if (uiState.isSearchActive) "Cerrar búsqueda" else stringResource(
                                R.string.search_tasks
                            )
                        )
                    }
                }
            )
            // Barra de búsqueda (aparece/desaparece)
            if (uiState.isSearchActive) {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::setSearchQuery,
                    onClose = { viewModel.toggleSearch() },
                    modifier = Modifier.padding(horizontal = Dimensions.SpacingMedium)
                )
            }
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
                // Filtros Dropdown
                TaskFilterDropdown(
                    currentFilter = uiState.currentFilter,
                    isExpanded = uiState.isFilterDropdownExpanded,
                    onFilterSelected = viewModel::setFilter,
                    onExpandedChange = viewModel::setFilterDropdownExpanded,
                    modifier = Modifier.padding(horizontal = Dimensions.SpacingMedium)
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

                // Lista de tareas
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
                            onTaskLongClick = { /* Mostrar menú contextual */ },
                            onToggleCompletion = { isCompleted ->
                                viewModel.toggleTaskCompletion(task.id, isCompleted)
                            },
                            onArchiveTask = {
                                viewModel.archiveTask(task.id)
                            }
                        )
                    }

                    // Estado vacío
                    if (tasks.isEmpty() && !uiState.isLoading) {
                        item {
                            TaskEmptyState(
                                currentFilter = uiState.currentFilter,
                                onCreateTask = onNavigateToCreateTask,
                                modifier = Modifier.fillParentMaxSize()
                            )
                        }
                    }
                }
            }

            // Overlay de carga
            if (uiState.isLoading) {
                HabitJourneyLoadingOverlay()
            }
        }
    }
}
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {  Text(stringResource(R.string.search_tasks_placeholder)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription =stringResource(R.string.clear_search)
                    )
                }
            }
        } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { keyboardController?.hide() }
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskFilterDropdown(
    currentFilter: TaskFilterType,
    isExpanded: Boolean,
    onFilterSelected: (TaskFilterType) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = onExpandedChange
        ) {
            OutlinedTextField(
                value = getFilterDisplayName(currentFilter),
                onValueChange = { },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                label = {Text(stringResource(R.string.filter_by)) },
                leadingIcon = {
                    Icon(
                        imageVector = getFilterIcon(currentFilter),
                        contentDescription = null,
                        tint = AcentoInformativo
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                },
                colors = OutlinedTextFieldDefaults.colors()
            )

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                TaskFilterType.entries.forEach { filter ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = getFilterIcon(filter),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (filter == currentFilter) AcentoInformativo else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = getFilterDisplayName(filter),
                                    color = if (filter == currentFilter) AcentoInformativo else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        onClick = {
                            onFilterSelected(filter)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun getFilterDisplayName(filter: TaskFilterType): String {
    return when (filter) {
        TaskFilterType.ALL -> stringResource(R.string.filter_all)
        TaskFilterType.ACTIVE -> stringResource(R.string.filter_active)
        TaskFilterType.COMPLETED -> stringResource(R.string.filter_completed)
        TaskFilterType.ARCHIVED -> stringResource(R.string.filter_archived)
        TaskFilterType.OVERDUE -> stringResource(R.string.filter_overdue)
    }
}

@Composable
private fun getFilterIcon(filter: TaskFilterType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (filter) {
        TaskFilterType.ALL -> Icons.AutoMirrored.Filled.List
        TaskFilterType.ACTIVE -> Icons.Default.Schedule
        TaskFilterType.COMPLETED -> Icons.Default.CheckCircle
        TaskFilterType.ARCHIVED -> Icons.Default.Archive
        TaskFilterType.OVERDUE -> Icons.Default.Warning
    }
}
