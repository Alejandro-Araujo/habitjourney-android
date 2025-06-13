package com.alejandro.habitjourney.features.dashboard.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alejandro.habitjourney.core.data.local.enums.NoteType
import com.alejandro.habitjourney.core.presentation.ui.components.*
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import com.alejandro.habitjourney.features.dashboard.presentation.viewmodel.DashboardViewModel
import com.alejandro.habitjourney.features.habit.domain.model.HabitWithLogs
import com.alejandro.habitjourney.features.habit.presentation.screen.HabitIconMapper
import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.note.domain.model.Note
import com.alejandro.habitjourney.features.task.presentation.components.TaskDateUtils
import com.alejandro.habitjourney.features.task.presentation.components.TaskPriorityIndicator
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import androidx.compose.ui.res.stringResource
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.features.dashboard.presentation.components.CalculationInfoDialog
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun DashboardScreen(
    onNavigateToHabits: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToCreateHabit: () -> Unit,
    onNavigateToCreateTask: () -> Unit,
    onNavigateToCreateNote: () -> Unit,
    onNavigateToHabitDetail: (Long) -> Unit,
    onNavigateToTaskDetail: (Long) -> Unit,
    onNavigateToNoteDetail: (Long) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    // Pull to refresh
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refreshDashboard() }
    )

    var showStatsInfo by remember { mutableStateOf(false) }

    // FAB expanded state
    var isFabExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        floatingActionButton = {
            ExpandableFAB(
                isExpanded = isFabExpanded,
                onToggle = { isFabExpanded = !isFabExpanded },
                onCreateHabit = {
                    isFabExpanded = false
                    onNavigateToCreateHabit()
                },
                onCreateTask = {
                    isFabExpanded = false
                    onNavigateToCreateTask()
                },
                onCreateNote = {
                    isFabExpanded = false
                    onNavigateToCreateNote()
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            when {
                uiState.isLoading && !isRefreshing -> {
                    HabitJourneyLoadingOverlay()
                }
                uiState.isEmpty && !uiState.isLoading -> {
                    EmptyDashboardState(
                        onCreateHabit = onNavigateToCreateHabit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                vertical = Dimensions.SpacingMedium
                            ),
                            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
                        ) {
                            // Welcome Header
                            WelcomeHeader(
                                userName = uiState.user?.name ?: "",
                                greeting = viewModel.greetingMessage
                            )

                            // Quick Stats
                            QuickStatsCard(
                                completedHabits = uiState.completedHabitsToday,
                                totalHabits = uiState.totalHabitsToday,
                                activeTasks = uiState.totalActiveTasks,
                                overdueTasks = uiState.overdueTasks,
                                currentStreak = uiState.currentStreak,
                                productivityScore = uiState.productivityScore,
                                summaryMessage = uiState.summaryMessage,
                                onShowStatsInfo = { showStatsInfo = true }

                            )
                        }

                        // CONTENIDO SCROLLEABLE
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = Dimensions.SpacingMedium,
                                end = Dimensions.SpacingMedium,
                                bottom = Dimensions.FabBottomPadding
                            ),
                            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
                        ) {
                            // Today's Habits
                            if (uiState.todayHabits.isNotEmpty()) {
                                item {
                                    SectionHeader(
                                        title = stringResource(R.string.dashboard_todays_habits),
                                        actionText = stringResource(R.string.dashboard_view_all),
                                        onActionClick = onNavigateToHabits
                                    )
                                }

                                item {
                                    TodayHabitsRow(
                                        habits = uiState.todayHabits,
                                        onHabitClick = onNavigateToHabitDetail,
                                        onToggleCompletion = { habitId, habitWithLogs ->
                                            viewModel.toggleHabitCompletion(habitId, habitWithLogs)
                                        }
                                    )
                                }
                            }

                            // Priority Tasks
                            if (uiState.activeTasks.isNotEmpty()) {
                                item {
                                    SectionHeader(
                                        title = stringResource(R.string.dashboard_pending_tasks),
                                        actionText = stringResource(R.string.dashboard_view_all),
                                        onActionClick = onNavigateToTasks
                                    )
                                }

                                items(uiState.activeTasks.take(3)) { task ->
                                    TaskQuickCard(
                                        task = task,
                                        onTaskClick = { onNavigateToTaskDetail(task.id) },
                                        onToggleCompletion = {
                                            viewModel.toggleTaskCompletion(task.id, task.isCompleted)
                                        }
                                    )
                                }
                            }

                            // Recent Notes
                            if (uiState.recentNotes.isNotEmpty()) {
                                item {
                                    SectionHeader(
                                        title = stringResource(R.string.dashboard_recent_notes),
                                        actionText = stringResource(R.string.dashboard_view_all),
                                        onActionClick = onNavigateToNotes
                                    )
                                }

                                items(uiState.recentNotes) { note ->
                                    NoteQuickCard(
                                        note = note,
                                        onNoteClick = { onNavigateToNoteDetail(note.id) }
                                    )
                                }
                            }

                            // Motivational Card
                            if (uiState.productivityScore >= 80 || uiState.currentStreak >= 7) {
                                item {
                                    MotivationalCard(
                                        score = uiState.productivityScore,
                                        message = uiState.motivationalQuote,
                                        streak = uiState.currentStreak
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Pull refresh indicator
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = AcentoInformativo
            )
        }

        if (showStatsInfo) {
            CalculationInfoDialog(
                onDismiss = { showStatsInfo = false }
            )
        }
    }


    // Error handling
    uiState.error?.let { errorMsg ->
        LaunchedEffect(errorMsg) {
            snackbarHostState.showSnackbar(
                message = errorMsg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
}

// Componentes auxiliares
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WelcomeHeader(
    userName: String,
    greeting: String,
    modifier: Modifier = Modifier
) {
    HabitJourneyCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                tint = AcentoInformativo,
                modifier = Modifier.size(Dimensions.IconSizeLarge)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (userName.isNotBlank()) {
                        "$greeting, $userName"
                    } else {
                        greeting
                    },
                    style = Typography.headlineMediumEmphasized,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

                Text(
                    text = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).let { dateTime ->
                        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                            .withLocale(Locale.getDefault())
                        val javaLocalDate = java.time.LocalDate.of(
                            dateTime.year,
                            dateTime.monthNumber,
                            dateTime.dayOfMonth
                        )
                        javaLocalDate.format(formatter)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuickStatsCard(
    completedHabits: Int,
    totalHabits: Int,
    activeTasks: Int,
    overdueTasks: Int,
    currentStreak: Int,
    productivityScore: Int,
    summaryMessage: String,
    onShowStatsInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    HabitJourneyCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_daily_summary),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )

                IconButton(
                    onClick = onShowStatsInfo,
                    modifier = Modifier.size(Dimensions.IconSizeNormal)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.stats_info_button_description),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Habits
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    value = stringResource(R.string.dashboard_habits_fraction, completedHabits, totalHabits),
                    label = stringResource(R.string.dashboard_habits_label),
                    color = AcentoPositivo
                )

                // Tasks
                StatItem(
                    icon = Icons.Default.Task,
                    value = activeTasks.toString(),
                    label = stringResource(R.string.dashboard_tasks_label),
                    color = AcentoInformativo
                )

                // Streak
                StatItem(
                    icon = Icons.Default.LocalFireDepartment,
                    value = currentStreak.toString(),
                    label = stringResource(R.string.dashboard_streak_label),
                    color = Logro
                )

                // Score
                StatItem(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    value = stringResource(R.string.dashboard_score_percentage, productivityScore),
                    label = stringResource(R.string.dashboard_score_label),
                    color = when {
                        productivityScore >= 80 -> AcentoPositivo
                        productivityScore >= 60 -> Logro
                        else -> AcentoUrgente
                    }
                )
            }

            // Summary message
            Text(
                text = summaryMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionHeader(
    modifier: Modifier = Modifier,
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )

        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    color = AcentoInformativo
                )
            }
        }
    }
}

@Composable
private fun TodayHabitsRow(
    habits: List<HabitWithLogs>,
    onHabitClick: (Long) -> Unit,
    onToggleCompletion: (Long, HabitWithLogs) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(habits) { habitWithLogs ->
            HabitQuickCard(
                habitWithLogs = habitWithLogs,
                onHabitClick = { onHabitClick(habitWithLogs.habit.id) },
                onToggleCompletion = {
                    onToggleCompletion(habitWithLogs.habit.id, habitWithLogs)
                }
            )
        }
    }
}

@Composable
private fun HabitQuickCard(
    habitWithLogs: HabitWithLogs,
    onHabitClick: () -> Unit,
    onToggleCompletion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habit = habitWithLogs.habit
    val isCompleted = habitWithLogs.isCompletedToday
    val progress = habitWithLogs.completionPercentageToday

    Card(
        modifier = modifier
            .width(140.dp)
            .height(160.dp),
        onClick = onHabitClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                AcentoPositivo.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCompleted) {0.dp} else 2.dp,
            pressedElevation =if (isCompleted) {0.dp} else 4.dp,
        ),
        border = if (isCompleted) {
            BorderStroke(1.dp, AcentoPositivo)
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimensions.SpacingSmall),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon and name
            Column {
                Icon(
                    imageVector = HabitIconMapper.getIconForHabitType(habit.type),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (isCompleted) AcentoPositivo else AcentoInformativo
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Progress or completion
            Column {
                if (habit.dailyTarget != null && habit.dailyTarget > 1) {
                    // Show progress for habits with targets
                    HabitJourneyProgressIndicator(
                        progress = progress / 100f,
                        type = HabitJourneyProgressType.LINEAR,
                        modifier = Modifier.fillMaxWidth(),
                        showLabel = true,
                        progressColor = if (isCompleted) AcentoPositivo else AcentoInformativo
                    )
                    Text(
                        text = stringResource(
                            R.string.dashboard_habit_progress,
                            habitWithLogs.todayProgress,
                            habit.dailyTarget
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    // Simple check for DO habits
                    IconButton(
                        onClick = onToggleCompletion,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = if (isCompleted) {
                                Icons.Default.CheckCircle
                            } else {
                                Icons.Default.RadioButtonUnchecked
                            },
                            contentDescription = null,
                            tint = if (isCompleted) AcentoPositivo else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskQuickCard(
    task: Task,
    onTaskClick: () -> Unit,
    onToggleCompletion: () -> Unit,
    modifier: Modifier = Modifier
) {
    HabitJourneyCard(
        onClick = onTaskClick,
        modifier = modifier,
        containerColor = if (task.isCompleted) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleCompletion() },
                colors = CheckboxDefaults.colors(
                    checkedColor = AcentoPositivo
                )
            )

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Dimensions.SpacingSmall)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.isCompleted) {
                        TextDecoration.LineThrough
                    } else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Due date or priority
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
                ) {
                    task.dueDate?.let { date ->
                        val isOverdue = date < Clock.System.todayIn(TimeZone.currentSystemDefault())
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(Dimensions.IconSizeSmall),
                            tint = if (isOverdue && !task.isCompleted) Error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = TaskDateUtils.formatDate(date),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isOverdue && !task.isCompleted) Error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    task.priority?.let { priority ->
                        TaskPriorityIndicator(
                            priority = priority
                        )
                    }
                }
            }

            // Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Dimensions.IconSizeSmall)
            )
        }
    }
}

@Composable
private fun NoteQuickCard(
    note: Note,
    onNoteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HabitJourneyCard(
        onClick = onNoteClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Note type icon
            Icon(
                imageVector = when (note.noteType) {
                    NoteType.TEXT -> Icons.Default.Description
                    NoteType.LIST -> Icons.Default.Checklist
                },
                contentDescription = null,
                tint = AcentoInformativo,
                modifier = Modifier.size(Dimensions.IconSizeNormal)
            )

            Spacer(modifier = Modifier.width(Dimensions.SpacingMedium))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = note.title.ifBlank { stringResource(R.string.note_untitled) },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = note.preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Stats
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (note.isFavorite) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.IconSizeSmall),
                        tint = AcentoPositivo
                    )
                }
                Text(
                    text = stringResource(R.string.dashboard_note_word_count, note.wordCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MotivationalCard(
    score: Int,
    message: String,
    streak: Int,
    modifier: Modifier = Modifier
) {
    HabitJourneyCard(
        modifier = modifier,
        containerColor = AcentoPositivo.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = Logro,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(Dimensions.SpacingMedium))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.dashboard_excellent_progress),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AcentoPositivo
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (streak >= 7) {
                    Text(
                        text = stringResource(R.string.dashboard_streak_days, streak),
                        style = MaterialTheme.typography.bodySmall,
                        color = Logro,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                text = stringResource(R.string.dashboard_score_percentage, score),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AcentoPositivo
            )
        }
    }
}

@Composable
private fun EmptyDashboardState(
    onCreateHabit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Dashboard,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        Text(
            text = stringResource(R.string.dashboard_welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

        Text(
            text = stringResource(R.string.dashboard_welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        HabitJourneyButton(
            text = stringResource(R.string.dashboard_create_first_habit),
            onClick = onCreateHabit,
            type = HabitJourneyButtonType.PRIMARY,
            leadingIcon = Icons.Default.Add,
            modifier = Modifier.fillMaxWidth(0.6f)
        )
    }
}

@Composable
private fun ExpandableFAB(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCreateHabit: () -> Unit,
    onCreateTask: () -> Unit,
    onCreateNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
    ) {
        // Mini FABs (visible when expanded)
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
            ) {
                // Note FAB
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(Dimensions.CornerRadius),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = stringResource(R.string.dashboard_fab_new_note),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = onCreateNote,
                        containerColor = AcentoInformativo
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Note, contentDescription = null)
                    }
                }

                // Task FAB
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(Dimensions.CornerRadius),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = stringResource(R.string.dashboard_fab_new_task),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = onCreateTask,
                        containerColor = AcentoUrgente
                    ) {
                        Icon(Icons.Default.Task, contentDescription = null)
                    }
                }

                // Habit FAB
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(Dimensions.CornerRadius),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = stringResource(R.string.dashboard_fab_new_habit),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = onCreateHabit,
                        containerColor = AcentoPositivo
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                    }
                }
            }
        }

        // Main FAB
        FloatingActionButton(
            onClick = onToggle,
            containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant else AcentoInformativo
        ) {
            AnimatedContent(
                targetState = isExpanded,
                label = "FAB Icon"
            ) { expanded ->
                Icon(
                    imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (expanded) {
                        stringResource(R.string.dashboard_fab_close)
                    } else {
                        stringResource(R.string.dashboard_fab_create_new)
                    },
                    modifier = Modifier.graphicsLayer {
                        rotationZ = if (expanded) 45f else 0f
                    }
                )
            }
        }
    }
}