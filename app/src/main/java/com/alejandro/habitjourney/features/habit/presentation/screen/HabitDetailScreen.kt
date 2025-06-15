package com.alejandro.habitjourney.features.habit.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.IncompleteCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restore
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
import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import com.alejandro.habitjourney.core.presentation.ui.components.ConfirmationDialog
import com.alejandro.habitjourney.core.presentation.ui.components.ErrorDialog
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyItem
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyLoadingOverlay
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyProgressIndicator
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyProgressType
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoInformativo
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoPositivo
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions
import com.alejandro.habitjourney.core.utils.formatter.displayName
import com.alejandro.habitjourney.features.habit.domain.model.HabitLog
import com.alejandro.habitjourney.features.habit.presentation.components.HabitContextMenu
import com.alejandro.habitjourney.features.habit.presentation.viewmodel.HabitDetailViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.todayIn
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Pantalla que muestra la vista detallada de un hábito específico.
 *
 * Esta pantalla es responsable de cargar y presentar toda la información de un hábito,
 * incluyendo sus detalles de configuración, el progreso general y el historial de registros.
 * También proporciona acciones como editar, archivar u omitir el hábito.
 *
 * @param habitId El ID del hábito que se debe cargar y mostrar.
 * @param onNavigateBack Callback para manejar la acción de volver a la pantalla anterior.
 * @param onNavigateToEditHabit Callback para navegar a la pantalla de edición del hábito.
 * @param viewModel El [HabitDetailViewModel] que gestiona el estado y la lógica de esta pantalla.
 */
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
                        if (habit.isArchived) viewModel.archiveHabit()
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

/**
 * Componente privado para renderizar una sección con título dentro de la pantalla de detalle.
 * @param title El título de la sección.
 * @param content El contenido Composable de la sección.
 */
@Composable
private fun HabitDetailSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.SpacingSmall)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
        content()
    }
}

/**
 * Componente privado que muestra las estadísticas de progreso del hábito.
 * Muestra el progreso de hoy y el progreso general (histórico).
 *
 * @param logs La lista completa de registros para calcular el progreso.
 * @param todayProgress El progreso de hoy como un valor flotante (0.0 a 1.0).
 * @param overallProgress El progreso general como un valor flotante (0.0 a 1.0).
 * @param modifier Modificador para personalizar el layout.
 */
@Composable
private fun HabitProgressSection(
    logs: List<HabitLog>,
    todayProgress: Float,
    overallProgress: Float,
    modifier: Modifier = Modifier
) {
    val totalLogs = logs.size
    val completedLogs = logs.count { it.status == LogStatus.COMPLETED }
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val todayLog = logs.find { it.date == today }

    Column(modifier = modifier) {
        // Progreso de hoy
        Text(
            text = stringResource(R.string.habit_detail_today_progress),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (todayLog != null) {
                    when (todayLog.status) {
                        LogStatus.COMPLETED -> stringResource(R.string.status_completed)
                        LogStatus.PARTIAL -> stringResource(R.string.status_partial, (todayLog.value ?: 0f).toInt())
                        LogStatus.NOT_COMPLETED -> stringResource(R.string.status_not_completed)
                        LogStatus.SKIPPED -> stringResource(R.string.log_status_skipped)
                        LogStatus.MISSED -> stringResource(R.string.log_status_missed)
                    }
                } else {
                    stringResource(R.string.status_not_started)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = stringResource(R.string.progress_percentage_format, (todayProgress * 100).toInt()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

        HabitJourneyProgressIndicator(
            progress = todayProgress,
            type = HabitJourneyProgressType.LINEAR,
            progressColor = AcentoPositivo,
            showLabel = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

        // Progreso general (histórico)
        Text(
            text = stringResource(R.string.habit_detail_overall_progress),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

        Text(
            text = stringResource(R.string.habit_detail_completion_rate, completedLogs, totalLogs),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

        HabitJourneyProgressIndicator(
            progress = overallProgress,
            type = HabitJourneyProgressType.LINEAR,
            progressColor = AcentoInformativo,
            showLabel = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
    }
}

/**
 * Componente privado que muestra el historial de registros de un hábito.
 *
 * @param logs La lista de [HabitLog] a mostrar.
 * @param modifier Modificador para personalizar el layout.
 */
@Composable
private fun HabitHistorySection(
    logs: List<HabitLog>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (logs.isEmpty()) {
            Text(
                text = stringResource(R.string.habit_detail_no_history),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        } else {
            logs.forEach { log ->
                val logStatusText = stringResource(
                    when (log.status) {
                        LogStatus.COMPLETED -> R.string.log_status_completed
                        LogStatus.SKIPPED -> R.string.log_status_skipped
                        LogStatus.MISSED -> R.string.log_status_missed
                        LogStatus.PARTIAL -> R.string.log_status_partial
                        LogStatus.NOT_COMPLETED -> R.string.log_status_not_completed
                    }
                ) + (if (log.value != null && log.value != 0f) " (${log.value.toInt()})" else "")

                HabitJourneyItem(
                    title = log.date.toJavaLocalDate().format(DateTimeFormatter.ofPattern("dd MMM")),
                    subtitle = logStatusText,
                    leadingContent = {
                        val icon = when (log.status) {
                            LogStatus.COMPLETED -> Icons.Default.CheckCircle
                            LogStatus.SKIPPED -> Icons.Default.Restore
                            LogStatus.MISSED -> Icons.Default.Info
                            LogStatus.PARTIAL -> Icons.Default.Info
                            LogStatus.NOT_COMPLETED -> Icons.Default.IncompleteCircle
                        }
                        Icon(imageVector = icon, contentDescription = null, tint = AcentoInformativo)
                    },
                    onClick = null,
                    containerColor = MaterialTheme.colorScheme.surface,
                    elevation = 0.dp
                )
            }
        }
    }
}
