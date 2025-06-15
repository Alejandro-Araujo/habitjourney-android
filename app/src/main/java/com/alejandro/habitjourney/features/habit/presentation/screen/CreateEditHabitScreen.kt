package com.alejandro.habitjourney.features.habit.presentation.screen

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
import com.alejandro.habitjourney.core.data.local.enums.Weekday
import com.alejandro.habitjourney.core.presentation.ui.components.ErrorDialog
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButton
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyTextField
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyLoadingOverlay
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyMultiSelectionDialog
import com.alejandro.habitjourney.core.presentation.ui.components.SuccessDialog
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import com.alejandro.habitjourney.features.habit.presentation.components.HabitSelectionDialog
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DatePickerDefaults
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButtonType
import com.alejandro.habitjourney.core.utils.formatter.DateTimeFormatters
import com.alejandro.habitjourney.core.utils.formatter.displayName
import com.alejandro.habitjourney.features.habit.presentation.components.SelectionButton
import kotlinx.datetime.Clock


/**
 * Pantalla para crear un nuevo hábito o editar uno existente.
 *
 * Esta pantalla presenta un formulario con todos los campos necesarios para configurar
 * un hábito, como nombre, descripción, frecuencia y fechas. Se adapta para mostrar
 * un título y un botón de guardado diferentes dependiendo de si se está creando o editando.
 *
 * @param habitId El ID del hábito a editar. Si es `null` o `0`, la pantalla entra en modo de creación.
 * @param onNavigateBack Callback para navegar a la pantalla anterior, generalmente después de guardar o cancelar.
 * @param viewModel El [CreateEditHabitViewModel] que gestiona el estado (UIState) y la lógica del formulario.
 */
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
                            tint = OnPrimary
                        )
                    }
                },
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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

                // Frecuencia (Selector)
                SelectionButton(
                    label = stringResource(R.string.habit_frequency_label),
                    selectedValue = when (uiState.frequency) {
                        "daily" -> stringResource(R.string.frequency_daily)
                        "weekly" -> stringResource(R.string.frequency_weekly)
                        else -> uiState.frequency.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase() else it.toString()
                        }
                    },
                    onClick = { showFrequencySelectionDialog = true }, // <-- Abre tu diálogo existente
                    icon = Icons.Default.DateRange,
                    enabled = true, // o basado en tu uiState
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

                // Días de la semana para frecuencia semanal
                if (uiState.frequency == "weekly") {
                    SelectionButton(
                        label = stringResource(R.string.habit_frequency_days_label),
                        selectedValue = if (uiState.frequencyDays.isEmpty()) {
                            stringResource(R.string.select_days_placeholder)
                        } else {
                            uiState.frequencyDays
                                .map { it.displayName(context) }
                                .sorted()
                                .joinToString(", ")
                        },
                        onClick = { showFrequencyDaysSelectionDialog = true },
                        icon = Icons.Default.DateRange,
                        enabled = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
                }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    SelectionButton(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.habit_start_date_label),
                        selectedValue = uiState.startDate?.let { DateTimeFormatters.formatDateLocalized(it) }
                            ?: stringResource(R.string.select_date_placeholder),
                        onClick = { showStartDatePicker = true },
                        icon = Icons.Default.DateRange,
                        enabled = true
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    SelectionButton(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.habit_end_date_label),
                        selectedValue = uiState.endDate?.let { DateTimeFormatters.formatDateLocalized(it) }
                            ?: stringResource(R.string.select_date_placeholder),
                        onClick = { showEndDatePicker = true },
                        icon = Icons.Default.DateRange,
                        enabled = true
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
                    text = if (habitId != null) {
                        stringResource(R.string.save_changes)
                    } else {
                        stringResource(R.string.create_habit_title)
                    },
                    onClick = {
                        viewModel.saveHabit(onSuccess = onNavigateBack)
                    },
                    type = HabitJourneyButtonType.PRIMARY,
                    enabled = uiState.isValid && !uiState.isSaving,
                    isLoading = uiState.isSaving,
                    leadingIcon = Icons.Default.Save,
                    iconContentDescription = stringResource(R.string.action_save),
                    modifier = Modifier.fillMaxWidth()
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

        // Diálogo de selección de Frecuencia
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

        // DatePicker para Fecha de Inicio
        if (showStartDatePicker) {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val initialStartDateMillis = uiState.startDate
                ?.atStartOfDayIn(TimeZone.UTC)
                ?.toEpochMilliseconds()
                ?: today.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = initialStartDateMillis,
                initialDisplayedMonthMillis = initialStartDateMillis
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
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        selectedDayContainerColor = AcentoInformativo,
                        todayDateBorderColor = AcentoInformativo,
                        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                        todayContentColor = AcentoInformativo,
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        // DatePicker para Fecha de Fin
        if (showEndDatePicker) {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val minDate = uiState.startDate ?: today
            val minDateMillis = minDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

            val initialEndDateMillis = uiState.endDate
                ?.atStartOfDayIn(TimeZone.UTC)
                ?.toEpochMilliseconds()
                ?: minDateMillis

            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = initialEndDateMillis,
                initialDisplayedMonthMillis = initialEndDateMillis
            )
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Instant.fromEpochMilliseconds(millis)
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .date
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
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        selectedDayContainerColor = AcentoInformativo,
                        todayDateBorderColor = AcentoInformativo,
                        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                        todayContentColor = AcentoInformativo,
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    }
}
