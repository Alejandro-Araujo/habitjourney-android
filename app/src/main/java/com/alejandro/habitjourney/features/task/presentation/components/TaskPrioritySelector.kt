package com.alejandro.habitjourney.features.task.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.Priority
import com.alejandro.habitjourney.core.presentation.ui.theme.*



@Composable
fun TaskPrioritySelector(
    priority: Priority?,
    onPriorityChange: (Priority?) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var showPriorityMenu by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.priority),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { if (enabled) showPriorityMenu = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AcentoInformativo
                    )
                ) {
                    priority?.let { p ->
                        TaskPriorityIcon(priority = p)
                        Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
                    }
                    Text(
                        text = priority?.let { getPriorityText(it) }
                            ?: stringResource(R.string.select_priority),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                DropdownMenu(
                    expanded = showPriorityMenu,
                    onDismissRequest = { showPriorityMenu = false }
                ) {
                    Priority.entries.forEach { p ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TaskPriorityIcon(priority = p)
                                    Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
                                    Text(
                                        text = getPriorityText(p),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            },
                            onClick = {
                                onPriorityChange(p)
                                showPriorityMenu = false
                            }
                        )
                    }

                    // Opción para limpiar prioridad
                    if (priority != null) {
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = null,
                                        modifier = Modifier.size(Dimensions.IconSizeButton),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
                                    Text(
                                        text = stringResource(R.string.no_priority),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            },
                            onClick = {
                                onPriorityChange(null)
                                showPriorityMenu = false
                            }
                        )
                    }
                }
            }

            if (priority != null && enabled) {
                IconButton(
                    onClick = { onPriorityChange(null) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.clear_priority),
                        tint = Error
                    )
                }
            }
        }
    }
}

@Composable
fun TaskPriorityIcon(
    priority: Priority,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = when (priority) {
            Priority.HIGH -> Icons.Default.KeyboardArrowUp
            Priority.MEDIUM -> Icons.Default.Remove
            Priority.LOW -> Icons.Default.KeyboardArrowDown
        },
        contentDescription = null,
        modifier = modifier.size(Dimensions.IconSizeButton),
        tint = when (priority) {
            Priority.HIGH -> AcentoUrgente
            Priority.MEDIUM -> Logro
            Priority.LOW -> AcentoPositivo
        }
    )
}

// Funciones de utilidad
@Composable
private fun getPriorityText(priority: Priority): String {
    return when (priority) {
        Priority.HIGH -> stringResource(R.string.priority_high)
        Priority.MEDIUM -> stringResource(R.string.priority_medium)
        Priority.LOW -> stringResource(R.string.priority_low)
    }
}