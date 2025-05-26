package com.alejandro.habitjourney.features.habit.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.IncompleteCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import com.alejandro.habitjourney.features.habit.domain.model.HabitLog // Importación CORREGIDA a HabitLog
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyItem
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoInformativo
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HabitHistorySection(
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
                        val icon = when(log.status) {
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