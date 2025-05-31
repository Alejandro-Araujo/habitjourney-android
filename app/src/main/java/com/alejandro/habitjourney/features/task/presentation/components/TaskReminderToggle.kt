package com.alejandro.habitjourney.features.task.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import kotlinx.datetime.*

@Composable
fun TaskReminderToggle(
    isReminderEnabled: Boolean,
    reminderDateTime: LocalDateTime?,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderDateTimeChange: (LocalDateTime?) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var showDateTimePicker by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.reminder),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Switch(
                checked = isReminderEnabled,
                onCheckedChange = onReminderEnabledChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AcentoPositivo,
                    checkedTrackColor = AcentoPositivo.copy(alpha = 0.54f)
                )
            )
        }

        if (isReminderEnabled) {
            Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { if (enabled) showDateTimePicker = true },
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AcentoInformativo
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.IconSizeButton)
                    )
                    Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
                    Text(
                        text = reminderDateTime?.let {
                            "${TaskDateUtils.formatDateForDisplay(it.date)} ${String.format("%02d:%02d", it.hour, it.minute)}"
                        } ?: stringResource(R.string.select_reminder_time),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                if (reminderDateTime != null && enabled) {
                    IconButton(
                        onClick = { onReminderDateTimeChange(null) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.clear_reminder),
                            tint = Error
                        )
                    }
                }
            }
        }
    }

    // Mostrar picker combinado de fecha y hora
    if (showDateTimePicker) {
        TaskDateTimePickerDialog(
            onDateTimeSelected = { dateTime ->
                onReminderDateTimeChange(dateTime)
                showDateTimePicker = false
            },
            onDismiss = { showDateTimePicker = false },
            initialDateTime = reminderDateTime ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )
    }
}