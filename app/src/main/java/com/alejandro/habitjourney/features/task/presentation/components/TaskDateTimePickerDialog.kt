package com.alejandro.habitjourney.features.task.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButton
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButtonType
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import kotlinx.datetime.*
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDateTimePickerDialog(
    onDateTimeSelected: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit,
    initialDateTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
) {
    var selectedDate by remember {
        mutableStateOf(initialDateTime.date)
    }
    var selectedTime by remember {
        mutableStateOf(initialDateTime.time)
    }
    var showingDatePicker by remember { mutableStateOf(true) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.toEpochDays().toLong() * 24 * 60 * 60 * 1000
    )

    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime.hour,
        initialMinute = selectedTime.minute
    )
    val formattedDate = TaskDateUtils.formatDateForDisplay(selectedDate)
    val formattedTime = String.format(Locale.getDefault(),"%02d:%02d", selectedTime.hour, selectedTime.minute)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            shape = RoundedCornerShape(Dimensions.CornerRadiusLarge),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(
                    top = Dimensions.SpacingLarge,
                    bottom = Dimensions.SpacingLarge,
                    start = 12.dp,
                    end = 12.dp
                )
            ) {
                // Tabs para cambiar entre fecha y hora
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
                ) {
                    FilterChip(
                        selected = showingDatePicker,
                        onClick = { showingDatePicker = true },
                        label = { Text(stringResource(R.string.date_label)) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AcentoInformativo,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )

                    FilterChip(
                        selected = !showingDatePicker,
                        onClick = { showingDatePicker = false },
                        label = { Text(stringResource(R.string.time_label)) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AcentoInformativo,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (showingDatePicker) {
                        DatePicker(
                            state = datePickerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 2.dp, end = 6.dp),
                            colors = DatePickerDefaults.colors(
                                selectedDayContainerColor = AcentoInformativo,
                                todayDateBorderColor = AcentoInformativo,
                                dayContentColor = MaterialTheme.colorScheme.onSurface,
                                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                                todayContentColor = AcentoInformativo
                            )
                        )
                    } else {
                        TimePicker(
                            state = timePickerState,
                            modifier = Modifier.padding(horizontal = 8.dp),
                            colors = TimePickerDefaults.colors(
                                selectorColor = AcentoInformativo,
                                periodSelectorSelectedContainerColor = AcentoInformativo
                            )
                        )
                    }
                }


                Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

                // Resumen de fecha y hora seleccionada
                Text(
                    text = stringResource(R.string.task_reminder_text, formattedDate, formattedTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
                ) {
                    HabitJourneyButton(
                        text = stringResource(R.string.cancel),
                        onClick = onDismiss,
                        type = HabitJourneyButtonType.SECONDARY,
                        modifier = Modifier.weight(1f)
                    )

                    HabitJourneyButton(
                        text = stringResource(R.string.ok),
                        onClick = {
                            // Actualizar fecha y hora seleccionadas
                            datePickerState.selectedDateMillis?.let { millis ->
                                val instant = Instant.fromEpochMilliseconds(millis)
                                selectedDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                            }
                            selectedTime = LocalTime(timePickerState.hour, timePickerState.minute)

                            // Crear LocalDateTime combinado
                            val dateTime = LocalDateTime(selectedDate, selectedTime)
                            onDateTimeSelected(dateTime)
                        },
                        type = HabitJourneyButtonType.PRIMARY,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}


