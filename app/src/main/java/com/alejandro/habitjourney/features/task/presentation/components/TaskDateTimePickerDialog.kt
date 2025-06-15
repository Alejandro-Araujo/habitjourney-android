package com.alejandro.habitjourney.features.task.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import kotlinx.datetime.*


/**
 * Un diálogo Composable que permite al usuario seleccionar una fecha y una hora.
 *
 * Combina un [DatePicker] y un [TimePicker] de Material Design 3,
 * permitiendo al usuario alternar entre la selección de fecha y hora.
 * La selección inicial puede ser proporcionada, y los resultados finales se devuelven al confirmar.
 *
 * @param onDateTimeSelected Lambda que se invoca cuando el usuario confirma la fecha y hora seleccionadas.
 * Recibe un objeto [LocalDateTime] con la fecha y hora combinadas.
 * @param onDismiss Lambda que se invoca cuando el diálogo se descarta sin confirmar una selección.
 * @param initialDateTime La [LocalDateTime] inicial a mostrar en los selectores. Por defecto, es la fecha y hora actual del sistema.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDateTimePickerDialog(
    onDateTimeSelected: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit,
    initialDateTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
) {
    val timeZone = TimeZone.currentSystemDefault()
    val initialDate = initialDateTime.date
    val initialTime = initialDateTime.time

    var selectedDate by remember { mutableStateOf(initialDate) }
    var selectedTime by remember { mutableStateOf(initialTime) }
    var showingDatePicker by remember { mutableStateOf(true) }


    val initialMillis = remember(initialDate) {
        initialDate.atStartOfDayIn(timeZone)
        val utcStartOfDay = initialDate.atStartOfDayIn(TimeZone.UTC)
        utcStartOfDay.toEpochMilliseconds()
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        initialDisplayedMonthMillis = initialMillis
    )

    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .width(360.dp)
                .heightIn(min = 500.dp, max = 600.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = Dimensions.SpacingLarge),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.select_date_and_time),
                        style = Typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(
                            top = Dimensions.SpacingLarge,
                            bottom = Dimensions.SpacingMedium
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (showingDatePicker) {
                        DatePicker(
                            state = datePickerState,
                            title = null,
                            headline = null,
                            showModeToggle = false,
                            colors = DatePickerDefaults.colors(
                                selectedDayContainerColor = AcentoInformativo,
                                todayDateBorderColor = AcentoInformativo,
                                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                                todayContentColor = AcentoInformativo,
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    } else {
                        TimePicker(
                            state = timePickerState,
                            modifier = Modifier.align(Alignment.Center),
                            colors = TimePickerDefaults.colors(
                                selectorColor = AcentoInformativo,
                                periodSelectorSelectedContainerColor = AcentoInformativo,
                                periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                                timeSelectorSelectedContainerColor = AcentoInformativo,
                                timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(
                            horizontal = Dimensions.SpacingSmall,
                            vertical = Dimensions.SpacingExtraSmall
                        ),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val instant = Instant.fromEpochMilliseconds(millis)
                                val utcDateTime = instant.toLocalDateTime(TimeZone.UTC)
                                selectedDate = utcDateTime.date
                            }

                            selectedTime = LocalTime(timePickerState.hour, timePickerState.minute)

                            val finalDateTime = LocalDateTime(selectedDate, selectedTime)
                            onDateTimeSelected(finalDateTime)
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = AcentoInformativo
                        )
                    ) {
                        Text(stringResource(R.string.action_confirm))
                    }
                }
            }
        }
    }
}