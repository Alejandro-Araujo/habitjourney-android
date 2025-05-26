package com.alejandro.habitjourney.core.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions

/**
 * Diálogo de selección múltiple genérico para una lista de opciones.
 * Permite al usuario seleccionar múltiples opciones de una lista predefinida.
 *
 * @param title El título del diálogo.
 * @param options La lista de strings que representan las opciones a elegir.
 * @param selectedOptions La lista de strings de las opciones actualmente seleccionadas.
 * @param onOptionSelected Callback que se invoca cuando el usuario confirma su selección,
 * pasando la lista actualizada de opciones seleccionadas.
 * @param onDismissRequest Callback que se invoca cuando se solicita cerrar el diálogo.
 */
@Composable
fun HabitJourneyMultiSelectionDialog(
    title: String,
    options: List<String>,
    selectedOptions: List<String>,
    onOptionSelected: (List<String>) -> Unit,
    onDismissRequest: () -> Unit
) {
    var tempSelectedOptions by remember { mutableStateOf(selectedOptions.toSet()) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    val isSelected = tempSelectedOptions.contains(option)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                tempSelectedOptions = if (isSelected) {
                                    tempSelectedOptions - option
                                } else {
                                    tempSelectedOptions + option
                                }
                            }
                            .padding(Dimensions.SpacingSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option,
                            modifier = Modifier.weight(1f),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onOptionSelected(tempSelectedOptions.toList())
                onDismissRequest()
            }) {
                Text(stringResource(R.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}