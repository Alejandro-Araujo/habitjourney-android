package com.alejandro.habitjourney.features.task.presentation.components

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import kotlinx.datetime.*

@Composable
fun TaskReminderToggle(
    modifier: Modifier = Modifier,
    isReminderEnabled: Boolean,
    reminderDateTime: LocalDateTime?,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderDateTimeChange: (LocalDateTime?) -> Unit,
    enabled: Boolean = true,
) {
    var showDateTimePicker by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isReminderEnabled) {
                AcentoInformativo.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (isReminderEnabled) {
            CardDefaults.outlinedCardBorder().copy(
                width = 2.dp,
                brush = androidx.compose.ui.graphics.SolidColor(AcentoInformativo)
            )
        } else null
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.SpacingMedium)
        ) {
            // Toggle principal
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = if (isReminderEnabled) AcentoInformativo else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(Dimensions.IconSizeNormal)
                )

                Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.reminder_label),
                        style = Typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(R.string.reminder_description),
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = isReminderEnabled,
                    onCheckedChange = onReminderEnabledChange,
                    enabled = enabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = AcentoInformativo,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            // Sección expandible para seleccionar fecha/hora
            AnimatedVisibility(
                visible = isReminderEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = Dimensions.SpacingSmall),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )

                    // Selector de fecha y hora
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Dimensions.CornerRadius))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable(enabled = enabled) { showDateTimePicker = true }
                            .padding(Dimensions.SpacingSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = AcentoInformativo,
                            modifier = Modifier.size(Dimensions.IconSizeSmall)
                        )

                        Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (reminderDateTime != null) {
                                    formatReminderDateTime(reminderDateTime)
                                } else {
                                    stringResource(R.string.select_reminder_time)
                                },
                                style = Typography.bodyMedium,
                                color = if (reminderDateTime != null) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )

                            if (reminderDateTime != null) {
                                Text(
                                    text = getRelativeTimeString(reminderDateTime),
                                    style = Typography.bodySmall,
                                    color = AcentoInformativo
                                )
                            }
                        }

                        if (reminderDateTime != null) {
                            IconButton(
                                onClick = { onReminderDateTimeChange(null) },
                                enabled = enabled
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.clear_reminder),
                                    tint = Error,
                                    modifier = Modifier.size(Dimensions.IconSizeSmall)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(Dimensions.IconSizeSmall)
                            )
                        }
                    }

                    // Sugerencias rápidas
                    if (reminderDateTime == null) {
                        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
                        ) {
                            QuickReminderChip(
                                text = stringResource(R.string.in_1_hour),
                                onClick = {
                                    val timeZone = TimeZone.currentSystemDefault()
                                    val nowAsInstant = Clock.System.now()
                                    val oneHourLaterAsInstant = nowAsInstant.plus(DateTimePeriod(hours = 1), timeZone)
                                    onReminderDateTimeChange(oneHourLaterAsInstant.toLocalDateTime(timeZone))
                                },
                                enabled = enabled
                            )

                            QuickReminderChip(
                                text = stringResource(R.string.tomorrow_9am),
                                onClick = {
                                    val tomorrow = Clock.System.now()
                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                        .date
                                        .plus(1, DateTimeUnit.DAY)
                                    onReminderDateTimeChange(
                                        LocalDateTime(tomorrow, LocalTime(9, 0))
                                    )
                                },
                                enabled = enabled
                            )

                            QuickReminderChip(
                                text = stringResource(R.string.next_week),
                                onClick = {
                                    val nextWeek = Clock.System.now()
                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                        .date
                                        .plus(7, DateTimeUnit.DAY)
                                    onReminderDateTimeChange(
                                        LocalDateTime(nextWeek, LocalTime(9, 0))
                                    )
                                },
                                enabled = enabled
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo selector de fecha y hora
    if (showDateTimePicker) {
        val initialDateTime = reminderDateTime ?: run {
            val timeZone = TimeZone.currentSystemDefault()
            val nowAsInstant = Clock.System.now()
            val oneHourLaterAsInstant = nowAsInstant.plus(DateTimePeriod(hours = 1), timeZone)
            oneHourLaterAsInstant.toLocalDateTime(timeZone)
        }

        TaskDateTimePickerDialog(
            onDateTimeSelected = { dateTime ->
                onReminderDateTimeChange(dateTime)
                showDateTimePicker = false
            },
            onDismiss = { showDateTimePicker = false },
            initialDateTime = initialDateTime
        )
    }
}

@Composable
private fun QuickReminderChip(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = text,
                style = Typography.bodySmall
            )
        },
        enabled = enabled,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        border = null
    )
}

@SuppressLint("DefaultLocale")
@Composable
private fun formatReminderDateTime(dateTime: LocalDateTime): String {
    val dateStr = TaskDateUtils.formatDateForDisplay(dateTime.date)
    val timeStr = String.format("%02d:%02d", dateTime.hour, dateTime.minute)
    return "$dateStr $timeStr"
}

@Composable
private fun getRelativeTimeString(dateTime: LocalDateTime): String {
    val now = Clock.System.now()
    val target = dateTime.toInstant(TimeZone.currentSystemDefault())
    val duration = target - now

    return when {
        duration.inWholeMinutes < 60 -> {
            stringResource(R.string.in_x_minutes, duration.inWholeMinutes.toInt())
        }
        duration.inWholeHours < 24 -> {
            stringResource(R.string.in_x_hours, duration.inWholeHours.toInt())
        }
        duration.inWholeDays < 7 -> {
            stringResource(R.string.in_x_days, duration.inWholeDays.toInt())
        }
        else -> {
            stringResource(R.string.in_x_weeks, (duration.inWholeDays / 7).toInt())
        }
    }
}