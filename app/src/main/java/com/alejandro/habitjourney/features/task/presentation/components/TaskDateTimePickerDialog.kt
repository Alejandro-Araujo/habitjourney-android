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
import com.alejandro.habitjourney.BuildConfig
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDateTimePickerDialog(
    onDateTimeSelected: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit,
    initialDateTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
) {
    // Asegurar que la fecha inicial sea correcta
    val timeZone = TimeZone.currentSystemDefault()
    val initialDate = initialDateTime.date
    val initialTime = initialDateTime.time

    var selectedDate by remember { mutableStateOf(initialDate) }
    var selectedTime by remember { mutableStateOf(initialTime) }
    var showingDatePicker by remember { mutableStateOf(true) }

    // Calcular los milisegundos correctamente para la fecha inicial
    // IMPORTANTE: DatePicker espera milisegundos en UTC
    val initialMillis = remember(initialDate) {
        // Convertir la fecha local a UTC medianoche
        val localStartOfDay = initialDate.atStartOfDayIn(timeZone)
        val utcStartOfDay = initialDate.atStartOfDayIn(TimeZone.UTC)
        utcStartOfDay.toEpochMilliseconds()
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        // Establecer el primer día visible para asegurar que se muestre el mes correcto
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
                    // Título del diálogo
                    Text(
                        text = stringResource(R.string.select_date_and_time),
                        style = Typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(
                            top = Dimensions.SpacingLarge,
                            bottom = Dimensions.SpacingMedium
                        )
                    )

                    // Selector de fecha/hora con chips
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
                            // Actualizar la fecha seleccionada desde el picker
                            datePickerState.selectedDateMillis?.let { millis ->
                                // IMPORTANTE: DatePicker devuelve milisegundos en UTC
                                val instant = Instant.fromEpochMilliseconds(millis)
                                // Interpretar como fecha UTC y luego convertir a LocalDate
                                val utcDateTime = instant.toLocalDateTime(TimeZone.UTC)
                                selectedDate = utcDateTime.date
                            }

                            // Actualizar la hora seleccionada
                            selectedTime = LocalTime(timePickerState.hour, timePickerState.minute)

                            // Combinar fecha y hora
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

// Función de debug para verificar fechas (opcional, puedes eliminarla en producción)
@Composable
private fun DebugDateInfo(date: LocalDate, millis: Long) {
    if (BuildConfig.DEBUG) {
        Column {
            Text("Date: $date", style = Typography.bodySmall)
            Text("Millis: $millis", style = Typography.bodySmall)
            Text("Converted back: ${Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault()).date}",
                style = Typography.bodySmall)
        }
    }
}