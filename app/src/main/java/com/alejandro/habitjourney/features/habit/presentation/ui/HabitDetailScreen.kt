package com.alejandro.habitjourney.features.habit.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.HabitType
import com.alejandro.habitjourney.core.presentation.ui.components.EditFab
import com.alejandro.habitjourney.core.presentation.ui.components.ErrorDialog
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyItem
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyLoadingOverlay
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions
import com.alejandro.habitjourney.features.habit.presentation.ui.components.HabitDetailSection
import com.alejandro.habitjourney.features.habit.presentation.ui.components.HabitHistorySection
import com.alejandro.habitjourney.features.habit.presentation.ui.components.HabitProgressSection
import com.alejandro.habitjourney.features.habit.presentation.viewmodel.HabitDetailViewModel
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEditHabit: (Long) -> Unit,
    viewModel: HabitDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current // Obtener el contexto para la localización

    LaunchedEffect(habitId) {
        viewModel.loadHabitDetail(habitId)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.habit_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            // Mostrar el FAB de edición solo si el hábito existe y NO está archivado
            if (uiState.habitWithLogs?.habit != null && !uiState.habitWithLogs!!.habit.isArchived) {
                EditFab(
                    onClick = { onNavigateToEditHabit(habitId) },
                    modifier = Modifier.padding(bottom = Dimensions.FabBottomPadding)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            if (uiState.isLoading) {
                HabitJourneyLoadingOverlay(modifier = Modifier.fillMaxSize())
            }

            if (uiState.error != null) {
                ErrorDialog(
                    onDismissRequest = { viewModel.clearError() },
                    message = uiState.error!!
                )
            }

            uiState.habitWithLogs?.let { habitWithLogs ->
                val habit = habitWithLogs.habit
                val logs = habitWithLogs.logs

                Column(
                    modifier = Modifier.padding(Dimensions.SpacingMedium)
                ) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

                    habit.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
                    }

                    // Información general
                    HabitDetailSection(
                        title = stringResource(R.string.habit_detail_section_info),
                        content = {
                            /*
                            HabitJourneyItem(
                                title = stringResource(R.string.habit_type_label),
                                subtitle = habit.type.displayName(context),
                                onClick = null,
                                containerColor = MaterialTheme.colorScheme.surface,
                                elevation = 0.dp
                            ) */
                            HabitJourneyItem(
                                title = stringResource(R.string.habit_frequency_label),
                                subtitle = when (habit.frequency) { // <--- Localizado
                                    "daily" -> stringResource(R.string.frequency_daily)
                                    "weekly" -> stringResource(R.string.frequency_weekly)
                                    "custom" -> stringResource(R.string.frequency_custom)
                                    else -> habit.frequency
                                },
                                onClick = null,
                                containerColor = MaterialTheme.colorScheme.surface,
                                elevation = 0.dp
                            )
                            habit.dailyTarget?.let {
                                HabitJourneyItem(
                                    title = stringResource(R.string.habit_daily_target_label),
                                    subtitle = it.toString(),
                                    onClick = null,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    elevation = 0.dp
                                )
                            }
                            // Mostrar días de la semana si aplica
                            if (habit.frequency == "weekly" || habit.frequency == "custom") {
                                habit.frequencyDays?.let { days ->
                                    if (days.isNotEmpty()) {
                                        HabitJourneyItem(
                                            title = stringResource(R.string.habit_frequency_days_label),
                                            subtitle = days.joinToString(separator = ", ") { it.displayName(context) }, // <--- Mostrar días
                                            onClick = null,
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            elevation = 0.dp
                                        )
                                    }
                                }
                            }
                            habit.startDate?.let {
                                HabitJourneyItem(
                                    title = stringResource(R.string.habit_start_date),
                                    subtitle = it.toJavaLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)), // <--- Formato localizado
                                    onClick = null,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    elevation = 0.dp
                                )
                            }
                            habit.endDate?.let {
                                HabitJourneyItem(
                                    title = stringResource(R.string.habit_end_date),
                                    subtitle = it.toJavaLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)), // <--- Formato localizado
                                    onClick = null,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    elevation = 0.dp
                                )
                            }
                            // Mostrar estado de archivado
                            HabitJourneyItem(
                                title = stringResource(R.string.habit_status_label),
                                subtitle = if (habit.isArchived) stringResource(R.string.habit_status_archived) else stringResource(R.string.habit_status_active),
                                onClick = null,
                                containerColor = MaterialTheme.colorScheme.surface,
                                elevation = 0.dp
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

                    // Sección de Progreso
                    HabitDetailSection(
                        title = stringResource(R.string.habit_detail_section_progress),
                        content = {
                            HabitProgressSection(
                                logs = logs,
                                todayProgress = uiState.todayProgress,
                                overallProgress = uiState.overallProgress
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

                    // Historial de Logs
                    HabitDetailSection(
                        title = stringResource(R.string.habit_detail_section_history),
                        content = {
                            HabitHistorySection(logs = logs)
                        }
                    )
                }
            }
        }
    }
}