package com.alejandro.habitjourney.features.habit.presentation.ui

import android.content.Context
import androidx.compose.ui.tooling.preview.Preview
import com.alejandro.habitjourney.core.presentation.ui.theme.HabitJourneyTheme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.text.input.KeyboardType
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.HabitType
import com.alejandro.habitjourney.core.data.local.enums.Weekday
import com.alejandro.habitjourney.core.presentation.ui.components.ErrorDialog
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButton
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyTextField
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyLoadingOverlay
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyMultiSelectionDialog
import com.alejandro.habitjourney.core.presentation.ui.components.SuccessDialog
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions
import com.alejandro.habitjourney.features.habit.presentation.ui.components.HabitSelectionDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.Alignment
import com.alejandro.habitjourney.features.habit.presentation.viewmodel.CreateEditHabitViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import androidx.compose.material.icons.filled.Clear
import androidx.compose.foundation.interaction.MutableInteractionSource
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditHabitScreen(
    habitId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: CreateEditHabitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Estados para los diálogos
    var showFrequencySelectionDialog by remember { mutableStateOf(false) }
    var showFrequencyDaysSelectionDialog by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Cargar hábito si es edición
    LaunchedEffect(habitId) {
        habitId?.let { id ->
            if (id > 0) {
                viewModel.loadHabitById(id)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (uiState.isEditing) R.string.edit_habit_title
                            else R.string.create_habit_title
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimensions.SpacingMedium)
                .verticalScroll(scrollState)
        ) {
            // Nombre del Hábito
            HabitJourneyTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = stringResource(R.string.habit_name_label),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = stringResource(R.string.content_description_habit_name_icon),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

            // Descripción del Hábito
            HabitJourneyTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = stringResource(R.string.habit_description_label),
                singleLine = false,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = stringResource(R.string.content_description_habit_description_icon),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

            // Daily Target
            HabitJourneyTextField(
                value = uiState.dailyTarget?.toString() ?: "",
                onValueChange = { newValue ->
                    val targetValue = newValue.toIntOrNull()
                    if (newValue.isEmpty() || (targetValue != null && targetValue > 0)) {
                        viewModel.updateDailyTarget(targetValue)
                    }
                },
                label = stringResource(R.string.habit_daily_target_quantity_label),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Numbers,
                        contentDescription = stringResource(R.string.content_description_habit_daily_target_icon),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

            // Frecuencia (Selector) - CORREGIDO: Ahora muestra las traducciones y es completamente clickeable
            HabitJourneyTextField(
                value = when (uiState.frequency) {
                    "daily" -> stringResource(R.string.frequency_daily)
                    "weekly" -> stringResource(R.string.frequency_weekly)
                    else -> uiState.frequency.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase() else it.toString()
                    }
                },
                onValueChange = { /* Read-only */ },
                label = stringResource(R.string.habit_frequency_label),
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = stringResource(R.string.content_description_habit_frequency_selector),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showFrequencySelectionDialog = true
                    }
            )
            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

            // Días de la semana para frecuencia semanal - CORREGIDO: Ahora es completamente clickeable
            if (uiState.frequency == "weekly") {
                HabitJourneyTextField(
                    value = if (uiState.frequencyDays.isEmpty()) {
                        stringResource(R.string.select_days_placeholder)
                    } else {
                        uiState.frequencyDays.joinToString(separator = ", ") { it.displayName(context) }
                    },
                    onValueChange = { /* Read-only */ },
                    label = stringResource(R.string.habit_frequency_days_label),
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.content_description_habit_frequency_days_selector),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showFrequencyDaysSelectionDialog = true
                        }
                )
                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
            }

            // Selector de Fecha de Inicio - CORREGIDO: Completamente clickeable
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HabitJourneyTextField(
                    value = uiState.startDate?.toJavaLocalDate()?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)) ?: "",
                    onValueChange = { /* Read-only */ },
                    label = stringResource(R.string.habit_start_date_label),
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.content_description_start_date_picker),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            showStartDatePicker = true
                        }
                )
                if (uiState.startDate != null) {
                    IconButton(onClick = { viewModel.updateStartDate(null) }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.action_clear_date),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

            // Selector de Fecha de Fin - CORREGIDO: Completamente clickeable
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HabitJourneyTextField(
                    value = uiState.endDate?.toJavaLocalDate()?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)) ?: "",
                    onValueChange = { /* Read-only */ },
                    label = stringResource(R.string.habit_end_date_label),
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.content_description_end_date_picker),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            showEndDatePicker = true
                        }
                )
                if (uiState.endDate != null) {
                    IconButton(onClick = { viewModel.updateEndDate(null) }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.action_clear_date),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

            // Botón Guardar
            HabitJourneyButton(
                text = stringResource(R.string.action_save),
                onClick = viewModel::saveHabit,
                isLoading = uiState.isLoading,
                enabled = uiState.isValid && !uiState.isLoading
            )
        }

        // Overlay de carga
        if (uiState.isLoading) {
            HabitJourneyLoadingOverlay(modifier = Modifier.fillMaxSize())
        }
    }

    // DIÁLOGOS

    // Diálogo de error
    if (uiState.error != null) {
        ErrorDialog(
            onDismissRequest = { viewModel.clearError() },
            message = uiState.error!!
        )
    }

    // Diálogo de éxito
    if (uiState.isSaved) {
        SuccessDialog(
            onDismissRequest = {
                viewModel.resetSaveState()
                onNavigateBack()
            },
            title = stringResource(R.string.habit_saved_success_title),
            message = stringResource(R.string.habit_saved_success_message)
        )
    }

    // Diálogo de selección de Frecuencia - ELIMINADO CUSTOM
    if (showFrequencySelectionDialog) {
        HabitSelectionDialog(
            title = stringResource(R.string.select_habit_frequency),
            options = listOf("daily", "weekly").map { frequency ->
                when (frequency) {
                    "daily" -> stringResource(R.string.frequency_daily)
                    "weekly" -> stringResource(R.string.frequency_weekly)
                    else -> frequency.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            },
            selectedOption = when (uiState.frequency) {
                "daily" -> stringResource(R.string.frequency_daily)
                "weekly" -> stringResource(R.string.frequency_weekly)
                else -> uiState.frequency.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            },
            onOptionSelected = { selectedDisplayName ->
                val selectedFrequency = when (selectedDisplayName) {
                    context.getString(R.string.frequency_daily) -> "daily"
                    context.getString(R.string.frequency_weekly) -> "weekly"
                    else -> selectedDisplayName.lowercase()
                }
                viewModel.updateFrequency(selectedFrequency)
                showFrequencySelectionDialog = false
            },
            onDismissRequest = { showFrequencySelectionDialog = false }
        )
    }

    // Diálogo de selección de Días de la Semana
    if (showFrequencyDaysSelectionDialog) {
        HabitJourneyMultiSelectionDialog(
            title = stringResource(R.string.select_frequency_days),
            options = Weekday.entries.map { it.displayName(context) },
            selectedOptions = uiState.frequencyDays.map { it.displayName(context) },
            onOptionSelected = { selectedDisplayNames ->
                val selectedDays = selectedDisplayNames.map { displayName ->
                    Weekday.entries.first { it.displayName(context) == displayName }
                }
                viewModel.updateFrequencyDays(selectedDays)
            },
            onDismissRequest = { showFrequencyDaysSelectionDialog = false }
        )
    }

    // DatePicker para Fecha de Inicio - CORREGIDO: Con límite de fecha mínima (hoy)
    if (showStartDatePicker) {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val todayMillis = today.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.startDate?.atStartOfDayIn(TimeZone.currentSystemDefault())?.toEpochMilliseconds()
                ?: todayMillis
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .date
                        // Solo permitir fechas de hoy en adelante
                        if (selectedDate >= today) {
                            viewModel.updateStartDate(selectedDate)
                        }
                    }
                    showStartDatePicker = false
                }) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // DatePicker para Fecha de Fin - CORREGIDO: Con límite de fecha mínima (hoy o fecha de inicio)
    if (showEndDatePicker) {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val minDate = uiState.startDate ?: today
        val minDateMillis = minDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.endDate?.atStartOfDayIn(TimeZone.currentSystemDefault())?.toEpochMilliseconds()
                ?: minDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .date
                        // Solo permitir fechas posteriores a la fecha de inicio (o hoy si no hay fecha de inicio)
                        if (selectedDate >= minDate) {
                            viewModel.updateEndDate(selectedDate)
                        }
                    }
                    showEndDatePicker = false
                }) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// Función de extensión para obtener el nombre legible de HabitType
fun HabitType.displayName(context: Context): String {
    return when (this) {
        HabitType.DO -> context.getString(R.string.habit_type_yes_no)
        HabitType.QUANTITATIVE -> context.getString(R.string.habit_type_quantitative)
        HabitType.TIMER -> context.getString(R.string.habit_type_timer)
    }
}

// Función de extensión para obtener el nombre legible de Weekday
fun Weekday.displayName(context: Context): String {
    return when (this) {
        Weekday.MONDAY -> context.getString(R.string.weekday_monday)
        Weekday.TUESDAY -> context.getString(R.string.weekday_tuesday)
        Weekday.WEDNESDAY -> context.getString(R.string.weekday_wednesday)
        Weekday.THURSDAY -> context.getString(R.string.weekday_thursday)
        Weekday.FRIDAY -> context.getString(R.string.weekday_friday)
        Weekday.SATURDAY -> context.getString(R.string.weekday_saturday)
        Weekday.SUNDAY -> context.getString(R.string.weekday_sunday)
    }
}