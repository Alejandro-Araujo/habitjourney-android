package com.alejandro.habitjourney.features.task.presentation.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.theme.*

/**
 * Un diálogo Composable para seleccionar una fecha.
 *
 * Este diálogo encapsula un [DatePicker] de Material Design 3,
 * proporcionando botones de confirmación y cancelación para la interacción del usuario.
 *
 * @param datePickerState El estado de [DatePickerState] que controla y observa la selección de la fecha.
 * @param onDateSelected Lambda que se invoca cuando el usuario confirma una fecha. Recibe los milisegundos de la fecha seleccionada (o `null` si no se selecciona ninguna).
 * @param onDismiss Lambda que se invoca cuando el diálogo se descarta sin confirmar una selección.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDatePickerDialog(
    datePickerState: DatePickerState,
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = AcentoInformativo
                )
            ) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(stringResource(R.string.cancel))
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