package com.alejandro.habitjourney.features.habit.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.AddHabitFab
import com.alejandro.habitjourney.core.presentation.ui.components.ErrorDialog
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyLoadingOverlay
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoInformativo
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions
import com.alejandro.habitjourney.features.habit.presentation.ui.components.HabitCard
import com.alejandro.habitjourney.features.habit.presentation.viewmodel.HabitListViewModel
import com.alejandro.habitjourney.features.habit.presentation.viewmodel.HabitListItemUiModel

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
            TopAppBar(
                title = { Text(stringResource(R.string.habit_list_screen_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            AddHabitFab(
                onClick = onNavigateToCreateHabit,
                modifier = Modifier.padding(bottom = Dimensions.FabBottomPadding)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Contenido principal
            if (uiState.isLoading) {
                HabitJourneyLoadingOverlay(
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimensions.SpacingMedium),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

                        // Filtro de hábitos de hoy
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Dimensions.SpacingSmall),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.habit_list_show_today_only),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Switch(
                                checked = uiState.showTodayOnly,
                                onCheckedChange = { viewModel.toggleShowTodayOnly() }
                            )
                        }
                        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
                    }

                    // Título de la lista
                    item {
                        Text(
                            text = stringResource(
                                if (uiState.showTodayOnly) R.string.habit_list_today_habits_title
                                else R.string.habit_list_all_habits_title
                            ),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
                    }

                    // Lista de hábitos o mensaje vacío
                    if (uiState.filteredHabits.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(
                                    if (uiState.showTodayOnly) R.string.habit_list_no_habits_today
                                    else R.string.habit_list_no_all_habits
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Dimensions.SpacingMedium)
                            )
                        }
                    } else {
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

                                // --- PARÁMETROS DE ESTADO ACTUALIZADOS PARA HabitCard ---
                                isCompletedToday = uiHabit.isCompletedToday,
                                isSkippedToday = uiHabit.isSkippedToday,
                                isPartialToday = uiHabit.isPartialToday, // Nuevo parámetro
                                isMissedToday = uiHabit.isMissedToday,   // Nuevo parámetro
                                isNotCompletedToday = uiHabit.isNotCompletedToday, // Nuevo parámetro
                                isArchived = uiHabit.isArchived, // Antes isActive, ahora isArchived

                                dailyTarget = uiHabit.dailyTarget,
                                currentCompletionCount = uiHabit.currentCompletionCount,
                                showTodayActions = uiState.showTodayOnly,

                                // --- LLAMADAS A LAS FUNCIONES DEL VIEWMYODEL ACTUALIZADAS ---
                                onIncrementProgress = {
                                    viewModel.incrementHabitProgress(uiHabit.id)
                                },
                                onDecrementProgress = {
                                    viewModel.decrementHabitProgress(uiHabit.id)
                                },
                                onMarkSkipped = {
                                    viewModel.markHabitAsSkipped(uiHabit.id)
                                },
                                onUndoSkipped = { // ¡NUEVO!
                                    viewModel.markHabitAsNotCompleted(uiHabit.id)
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

                    item {
                        Spacer(modifier = Modifier.height(Dimensions.FabBottomPadding + Dimensions.SpacingSmall))
                    }
                }
            }

            if (uiState.error != null) {
                ErrorDialog(
                    onDismissRequest = { viewModel.clearError() },
                    message = uiState.error!!
                )
            }
        }
    }
}