package com.alejandro.habitjourney.features.habit.presentation.components


import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.text.style.TextAlign
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoInformativo
import com.alejandro.habitjourney.core.presentation.ui.theme.Typography

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

                // Icono de flecha para indicar que es un selector
                /*Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )*/
            }
        }
    }
}