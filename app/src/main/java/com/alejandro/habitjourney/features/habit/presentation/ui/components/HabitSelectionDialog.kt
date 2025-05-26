package com.alejandro.habitjourney.features.habit.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions

/**
 * Diálogo de selección simple específico para opciones de Hábitos (Tipo de Hábito, Frecuencia, etc.).
 * Permite al usuario elegir una única opción de una lista predefinida.
 *
 * @param title El título del diálogo.
 * @param options La lista de strings que representan las opciones a elegir.
 * @param selectedOption La opción actualmente seleccionada (string).
 * @param onOptionSelected Callback que se invoca cuando el usuario selecciona una opción.
 * @param onDismissRequest Callback que se invoca cuando se solicita cerrar el diálogo.
 */
@Composable
fun HabitSelectionDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    Text(
                        text = option,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onOptionSelected(option)
                                onDismissRequest()
                            }
                            .padding(Dimensions.SpacingSmall),
                        color = if (option == selectedOption) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}