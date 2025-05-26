package com.alejandro.habitjourney.features.habit.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions
import com.alejandro.habitjourney.core.data.local.enums.LogStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCard(
    habitName: String,
    habitDescription: String?,
    icon: ImageVector,
    iconContentDescription: String,
    completionProgressPercentage: Float,
    onClick: () -> Unit,
    accentColor: Color,

    // --- PARÁMETROS DE ESTADO DEL UI MODEL ---
    logStatus: LogStatus,
    isCompletedToday: Boolean,
    isSkippedToday: Boolean,
    isPartialToday: Boolean,
    isMissedToday: Boolean,
    isNotCompletedToday: Boolean,
    isArchived: Boolean,

    dailyTarget: Int?,
    currentCompletionCount: Int,
    showTodayActions: Boolean = false,
    modifier: Modifier = Modifier,

    // --- CALLBACKS DEL VIEWMODEL ---
    onIncrementProgress: ((Float) -> Unit)? = null,
    onDecrementProgress: ((Float) -> Unit)? = null,
    onUndoSkipped: (() -> Unit)? = null,
    onMarkSkipped: (() -> Unit)? = null,

    onArchiveHabit: (() -> Unit)? = null,
    onUnarchiveHabit: (() -> Unit)? = null,

    // --- PROPIEDADES PARA HABILITAR/DESHABILITAR BOTONES ---
    canIncrementProgress: Boolean,
    canDecrementProgress: Boolean,
    canToggleSkipped: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isArchived -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                isSkippedToday -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                isCompletedToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                isPartialToday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                isMissedToday -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCompletedToday || isSkippedToday || isArchived) 2.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.SpacingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono del hábito
                Icon(
                    imageVector = icon,
                    contentDescription = iconContentDescription,
                    tint = accentColor,
                    modifier = Modifier.size(32.dp)
                )

                // Información del hábito
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = habitName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!habitDescription.isNullOrBlank()) {
                        Text(
                            text = habitDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // CORREGIDO: Mostrar progreso numérico con mejor lógica
                    when {
                        dailyTarget != null && dailyTarget > 1 -> {
                            Text(
                                text = stringResource(
                                    R.string.habit_progress_text,
                                    currentCompletionCount,
                                    dailyTarget
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = accentColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        dailyTarget == 1 -> {
                            Text(
                                text = if (currentCompletionCount >= 1) "✓ Completado" else "⏸ Pendiente",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (currentCompletionCount >= 1) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Indicador de estado del log
                when (logStatus) {
                    LogStatus.COMPLETED -> Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.content_description_completed),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    LogStatus.SKIPPED -> Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = stringResource(R.string.content_description_skipped),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    LogStatus.MISSED -> Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = stringResource(R.string.content_description_missed),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    LogStatus.PARTIAL -> Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = "Parcial",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    else -> Unit
                }
            }

            // CORREGIDO: Barra de progreso con mejor lógica de visualización
            if (!isSkippedToday && completionProgressPercentage > 0f) {
                Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
                LinearProgressIndicator(
                    progress = { (completionProgressPercentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = accentColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Acciones para hábitos de hoy
            if (showTodayActions && !isArchived) {
                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dailyTarget != null && dailyTarget > 1) {
                        // Botón decrementar
                        IconButton(
                            onClick = { onDecrementProgress?.invoke(1f) },
                            enabled = canDecrementProgress && onDecrementProgress != null
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = stringResource(R.string.action_decrease),
                                tint = if (canDecrementProgress) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }

                        // Contador
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(horizontal = Dimensions.SpacingSmall)
                        ) {
                            Text(
                                text = "$currentCompletionCount/$dailyTarget",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(
                                    horizontal = Dimensions.SpacingSmall,
                                    vertical = 4.dp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Botón incrementar
                        IconButton(
                            onClick = { onIncrementProgress?.invoke(1f) },
                            enabled = canIncrementProgress && onIncrementProgress != null
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.action_increase),
                                tint = if (canIncrementProgress) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    } else {
                        // Para hábitos simples (dailyTarget = null o 1), mostrar checkbox
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.habit_card_complete),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
                            Checkbox(
                                checked = isCompletedToday,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        onIncrementProgress?.invoke(1f)
                                    } else {
                                        onDecrementProgress?.invoke(1f)
                                    }
                                },
                                enabled = !isArchived && !isSkippedToday
                            )
                        }
                    }
                }
            }
        }

        // Menú Contextual
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            if (!isArchived) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_archive_habit)) },
                    onClick = {
                        onArchiveHabit?.invoke()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) }
                )
            } else {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_unarchive_habit)) },
                    onClick = {
                        onUnarchiveHabit?.invoke()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Unarchive, contentDescription = null) }
                )
            }

            if (!isCompletedToday && !isSkippedToday && canToggleSkipped) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_skip)) },
                    onClick = {
                        onMarkSkipped?.invoke()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.EventBusy, contentDescription = null) }
                )
            } else if (isSkippedToday) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_undo_skip)) },
                    onClick = {
                        onUndoSkipped?.invoke()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Remove, contentDescription = null) }
                )
            }
        }
    }
}