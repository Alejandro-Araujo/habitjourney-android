package com.alejandro.habitjourney.features.habit.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.ConfirmationDialog
import com.alejandro.habitjourney.core.presentation.ui.components.ErrorDialog
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyItem
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyLoadingOverlay
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions
import com.alejandro.habitjourney.features.habit.presentation.components.HabitContextMenu
import com.alejandro.habitjourney.features.habit.presentation.components.HabitDetailSection
import com.alejandro.habitjourney.features.habit.presentation.components.HabitHistorySection
import com.alejandro.habitjourney.features.habit.presentation.components.HabitProgressSection
import com.alejandro.habitjourney.features.habit.presentation.viewmodel.HabitDetailViewModel
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


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
    val context = LocalContext.current

    var showMenuDropdown by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }


    LaunchedEffect(habitId) {
        viewModel.loadHabitDetail(habitId)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.habit_detail_section_info),
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    if (uiState.habitWithLogs?.habit != null && !uiState.isLoading) {
                        // Botón de editar
                        IconButton(
                            onClick = { onNavigateToEditHabit(habitId) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit_habit_title)
                            )
                        }

                        // Menú con más opciones
                        Box {
                            IconButton(
                                onClick = { showMenuDropdown = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = stringResource(R.string.more_options)
                                )
                            }

                            uiState.habitWithLogs?.let { habitWithLogs ->
                                val habit = habitWithLogs.habit
                                HabitContextMenu(
                                    expanded = showMenuDropdown,
                                    onDismiss = { showMenuDropdown = false },
                                    isArchived = habit.isArchived,
                                    isSkippedToday = viewModel.isSkippedToday(),
                                    isCompletedToday = viewModel.isCompletedToday(),
                                    canToggleSkipped = viewModel.canToggleSkipped(),
                                    onArchiveHabit = {
                                        showArchiveDialog = true
                                        showMenuDropdown = false
                                    },
                                    onUnarchiveHabit = {
                                        showArchiveDialog = true
                                        showMenuDropdown = false
                                    },
                                    onMarkSkipped = {
                                        viewModel.markSkipped()
                                        showMenuDropdown = false
                                    },
                                    onUndoSkipped = {
                                        viewModel.undoSkipped()
                                        showMenuDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
            ) {
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
                                HabitJourneyItem(
                                    title = stringResource(R.string.habit_frequency_label),
                                    subtitle = when (habit.frequency) {
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
                                                subtitle = days.joinToString(separator = ", ") { it.displayName(context) },
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
                                        subtitle = it.toJavaLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
                                        onClick = null,
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        elevation = 0.dp
                                    )
                                }
                                habit.endDate?.let {
                                    HabitJourneyItem(
                                        title = stringResource(R.string.habit_end_date),
                                        subtitle = it.toJavaLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
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

            // Overlay de carga
            if (uiState.isLoading) {
                HabitJourneyLoadingOverlay(modifier = Modifier.fillMaxSize())
            }
        }

        // Diálogo de confirmación para archivar/desarchivar
        if (showArchiveDialog) {
            uiState.habitWithLogs?.habit?.let { habit ->
                ConfirmationDialog(
                    onDismissRequest = { showArchiveDialog = false },
                    title = if (!habit.isArchived) {
                        stringResource(R.string.title_archive_habit)
                    } else {
                        stringResource(R.string.title_unarchive_habit)
                    },
                    message = if (!habit.isArchived) {
                        stringResource(R.string.action_archive_habit)
                    } else {
                        stringResource(R.string.action_unarchive_habit)
                    },
                    onConfirm = {
                        if (habit.isArchived) viewModel.archiveHabit() else viewModel.archiveHabit()
                        showArchiveDialog = false
                        onNavigateBack()
                    },
                    confirmText = if (!habit.isArchived) {
                        stringResource(R.string.archive)
                    } else {
                        stringResource(R.string.unarchive)
                    },
                    cancelText = stringResource(R.string.cancel),
                    icon = Icons.Default.Archive
                )
            }
        }
    }
}



