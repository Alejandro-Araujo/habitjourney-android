package com.alejandro.habitjourney.features.habit.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoInformativo
import com.alejandro.habitjourney.core.presentation.ui.theme.Typography


/**
 * Un botón de selección reutilizable que muestra una etiqueta, un icono y el valor seleccionado.
 *
 * Este componente está diseñado para ser utilizado en formularios donde el usuario necesita
 * seleccionar un valor de una lista o un diálogo (ej: selector de frecuencia, selector de fecha).
 *
 * @param label La etiqueta de texto que se muestra sobre el botón para describir el campo.
 * @param selectedValue El valor actualmente seleccionado que se muestra dentro del botón.
 * @param onClick El callback que se invoca cuando el usuario pulsa el botón.
 * @param icon El [ImageVector] que se muestra a la izquierda del valor seleccionado.
 * @param enabled Controla si el botón está habilitado y es interactivo.
 * @param modifier El [Modifier] que se aplicará al componente.
 */
@Composable
fun SelectionButton(
    label: String,
    selectedValue: String,
    onClick: () -> Unit,
    icon: ImageVector,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Etiqueta de texto sobre el botón
        Text(
            text = label,
            style = Typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

        // Botón delineado que abre el diálogo
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = { if (enabled) onClick() },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AcentoInformativo
                )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(Dimensions.SpacingExtraSmall))

                Text(
                    text = selectedValue,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}