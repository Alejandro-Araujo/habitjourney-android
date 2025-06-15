package com.alejandro.habitjourney.features.habit.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.*
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import com.alejandro.habitjourney.features.habit.presentation.components.HabitCard
import com.alejandro.habitjourney.features.habit.presentation.state.HabitFilterType
import com.alejandro.habitjourney.features.habit.presentation.viewmodel.HabitListViewModel

/**
 * Pantalla principal que muestra la lista de hábitos del usuario.
 *
 * Permite al usuario ver, buscar y filtrar sus hábitos. Muestra una lista de [HabitCard]
 * y gestiona los estados de carga, vacío y error.
 *
 * @param onNavigateToCreateHabit Callback para navegar a la pantalla de creación de un nuevo hábito.
 * @param onNavigateToHabitDetail Callback para navegar a la pantalla de detalle de un hábito, pasando su ID.
 * @param viewModel El [HabitListViewModel] que gestiona el estado y la lógica de esta pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    onNavigateToCreateHabit: () -> Unit,
    onNavigateToHabitDetail: (Long) -> Unit,
    viewModel: HabitListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            HabitJourneySearchableTopBar(
                title = stringResource(R.string.habit_list_screen_title),
                isSearchActive = uiState.isSearchActive,
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::setSearchQuery,
                onSearchToggle = viewModel::toggleSearch,
                currentFilter = uiState.currentFilter,
                filterOptions = listOf(
                    FilterOption(
                        value = HabitFilterType.TODAY,
                        label = stringResource(R.string.filter_habits_today),
                        icon = Icons.Default.Today
                    ),
                    FilterOption(
                        value = HabitFilterType.ALL,
                        label = stringResource(R.string.filter_habits_all),
                        icon = Icons.AutoMirrored.Filled.List
                    ),
                    FilterOption(
                        value = HabitFilterType.COMPLETED,
                        label = stringResource(R.string.filter_habits_completed),
                        icon = Icons.Default.CheckCircle
                    ),
                    FilterOption(
                        value = HabitFilterType.PENDING,
                        label = stringResource(R.string.filter_habits_pending),
                        icon = Icons.Default.PendingActions
                    ),
                    FilterOption(
                        value = HabitFilterType.ARCHIVED,
                        label = stringResource(R.string.filter_habits_archived),
                        icon = Icons.Default.Archive
                    )
                ),
                onFilterSelected = viewModel::setFilter,
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            HabitJourneyFloatingActionButton(
                onClick = onNavigateToCreateHabit,
                icon = Icons.Default.Add,
                containerColor = AcentoInformativo,
                iconContentDescription = stringResource(R.string.add_habit_content_description)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.filteredHabits.isEmpty() && !uiState.isLoading) {
                HabitJourneyEmptyState(
                    icon = Icons.AutoMirrored.Filled.EventNote,
                    title = getEmptyStateTitle(uiState.currentFilter, uiState.searchQuery),
                    description = getEmptyStateMessage(uiState.currentFilter, uiState.searchQuery),
                    actionButtonText = if (uiState.searchQuery.isBlank() && uiState.currentFilter != HabitFilterType.ARCHIVED) {
                        stringResource(R.string.create_first_habit)
                    } else null,
                    onActionClick = if (uiState.searchQuery.isBlank() && uiState.currentFilter != HabitFilterType.ARCHIVED) {
                        onNavigateToCreateHabit
                    } else null
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = Dimensions.SpacingMedium,
                        start = Dimensions.SpacingMedium,
                        end = Dimensions.SpacingMedium,
                        bottom = Dimensions.FabBottomPadding + Dimensions.SpacingMedium
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
                ) {
                    items(
                        items = uiState.filteredHabits,
                        key = { habit -> habit.id }
                    ) { uiHabit ->
                        HabitCard(
                            habitName = uiHabit.name,
                            habitDescription = uiHabit.description,
                            icon = uiHabit.icon,
                            iconContentDescription = stringResource(
                                R.string.content_description_habit_icon,
                                uiHabit.name
                            ),
                            completionProgressPercentage = uiHabit.completionProgressPercentage,
                            onClick = { onNavigateToHabitDetail(uiHabit.id) },
                            accentColor = AcentoInformativo,
                            logStatus = uiHabit.logStatus,
                            isCompletedToday = uiHabit.isCompletedToday,
                            isSkippedToday = uiHabit.isSkippedToday,
                            isArchived = uiHabit.isArchived,
                            dailyTarget = uiHabit.dailyTarget,
                            currentCompletionCount = uiHabit.currentCompletionCount,
                            showTodayActions = uiState.currentFilter == HabitFilterType.TODAY ||
                                    uiState.currentFilter == HabitFilterType.PENDING ||
                                    uiState.currentFilter == HabitFilterType.COMPLETED,
                            onIncrementProgress = {
                                viewModel.incrementHabitProgress(uiHabit.id)
                            },
                            onDecrementProgress = {
                                viewModel.decrementHabitProgress(uiHabit.id)
                            },
                            onUndoSkipped = {
                                viewModel.markHabitAsNotCompleted(uiHabit.id)
                            },
                            onMarkSkipped = {
                                viewModel.markHabitAsSkipped(uiHabit.id)
                            },
                            onArchiveHabit = {
                                viewModel.toggleHabitArchived(uiHabit.id, true)
                            },
                            onUnarchiveHabit = {
                                viewModel.toggleHabitArchived(uiHabit.id, false)
                            },
                            canIncrementProgress = uiHabit.canIncrementProgress,
                            canDecrementProgress = uiHabit.canDecrementProgress,
                            canToggleSkipped = uiHabit.canToggleSkipped
                        )
                    }
                }
            }

            // Overlay de carga
            if (uiState.isLoading) {
                HabitJourneyLoadingOverlay()
            }

            // Diálogo de error
            if (uiState.error != null) {
                ErrorDialog(
                    onDismissRequest = { viewModel.clearError() },
                    message = uiState.error!!
                )
            }
        }
    }
}

/**
 * Función de utilidad privada que devuelve el título apropiado para el estado vacío.
 *
 * @param filter El [HabitFilterType] actualmente activo.
 * @param searchQuery El término de búsqueda actual.
 * @return Un [String] con el título localizado para mostrar.
 */
@Composable
private fun getEmptyStateTitle(filter: HabitFilterType, searchQuery: String): String {
    return if (searchQuery.isNotBlank()) {
        stringResource(R.string.no_habits_found_search)
    } else {
        when (filter) {
            HabitFilterType.TODAY -> stringResource(R.string.no_habits_today_title)
            HabitFilterType.ALL -> stringResource(R.string.no_habits_all_title)
            HabitFilterType.ARCHIVED -> stringResource(R.string.no_habits_archived_title)
            HabitFilterType.COMPLETED -> stringResource(R.string.no_habits_completed_title)
            HabitFilterType.PENDING -> stringResource(R.string.no_habits_pending_title)
        }
    }
}

/**
 * Función de utilidad privada que devuelve el mensaje descriptivo para el estado vacío.
 *
 * @param filter El [HabitFilterType] actualmente activo.
 * @param searchQuery El término de búsqueda actual.
 * @return Un [String] con el mensaje localizado para mostrar.
 */
@Composable
private fun getEmptyStateMessage(filter: HabitFilterType, searchQuery: String): String {
    return if (searchQuery.isNotBlank()) {
        stringResource(R.string.no_habits_found_search_message, searchQuery)
    } else {
        when (filter) {
            HabitFilterType.TODAY -> stringResource(R.string.no_habits_today_message)
            HabitFilterType.ALL -> stringResource(R.string.no_habits_all_message)
            HabitFilterType.ARCHIVED -> stringResource(R.string.no_habits_archived_message)
            HabitFilterType.COMPLETED -> stringResource(R.string.no_habits_completed_message)
            HabitFilterType.PENDING -> stringResource(R.string.no_habits_pending_message)
        }
    }
}