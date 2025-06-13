package com.alejandro.habitjourney.features.habit.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import com.alejandro.habitjourney.features.habit.domain.model.HabitLog
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyProgressIndicator
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyProgressType
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoInformativo
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoPositivo
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Composable
fun HabitProgressSection(
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
                        LogStatus.SKIPPED ->  stringResource(R.string.log_status_skipped)
                        LogStatus.MISSED ->  stringResource(R.string.log_status_missed)
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