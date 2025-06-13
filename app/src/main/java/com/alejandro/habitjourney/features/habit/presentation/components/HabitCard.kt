package com.alejandro.habitjourney.features.habit.presentation.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import com.alejandro.habitjourney.core.presentation.ui.components.ConfirmationDialog

@Composable
fun HabitCard(
    modifier: Modifier = Modifier,
    habitName: String,
    habitDescription: String?,
    icon: ImageVector,
    iconContentDescription: String,
    completionProgressPercentage: Float,
    onClick: () -> Unit,
    accentColor: Color,

    // Parámetros de estado del UI Model
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

    // Callbacks del ViewModel
    onIncrementProgress: ((Float) -> Unit)? = null,
    onDecrementProgress: ((Float) -> Unit)? = null,
    onUndoSkipped: (() -> Unit)? = null,
    onMarkSkipped: (() -> Unit)? = null,
    onArchiveHabit: (() -> Unit)? = null,
    onUnarchiveHabit: (() -> Unit)? = null,

    // Propiedades para habilitar/deshabilitar botones
    canIncrementProgress: Boolean,
    canDecrementProgress: Boolean,
    canToggleSkipped: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }


    Card(
        modifier = modifier.combinedClickable(
            onClick = onClick,
            onLongClick = { showMenu = true }
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isArchived) {
                InactivoDeshabilitado.copy(alpha = AlphaValues.DisabledAlpha)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp,
            hoveredElevation = 3.dp
        ),
        shape = RoundedCornerShape(Dimensions.CornerRadius)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.SpacingMedium)
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
                    modifier = Modifier.size(Dimensions.IconSizeLarge)
                )

                // Información del hábito
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = habitName,
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!habitDescription.isNullOrBlank()) {
                        Text(
                            text = habitDescription,
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaValues.HighAlpha),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Mostrar progreso numérico
                    when {
                        dailyTarget != null && dailyTarget > 1 -> {
                            val (stringId, textColor) = when {
                                currentCompletionCount < 1 -> Pair(
                                    R.string.habit_pending_today,
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaValues.MediumAlpha)
                                )
                                currentCompletionCount == dailyTarget -> Pair(
                                    R.string.habit_completed_today,
                                    accentColor
                                )
                                else -> Pair(
                                    R.string.habit_progress_text,
                                    MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = stringResource(stringId, currentCompletionCount, dailyTarget),
                                style = Typography.labelMedium,
                                color = textColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        dailyTarget == 1 -> {
                            Text(
                                text = if (currentCompletionCount >= 1) {
                                    stringResource(R.string.habit_completed_today)
                                } else {
                                    stringResource(R.string.habit_pending_today)
                                },
                                style = Typography.labelMedium,
                                color = if (currentCompletionCount >= 1) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaValues.MediumAlpha),
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
                        tint = AcentoPositivo,
                        modifier = Modifier.size(Dimensions.IconSizeNormal)
                    )
                    LogStatus.SKIPPED -> Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = stringResource(R.string.content_description_skipped),
                        tint = AcentoUrgente,
                        modifier = Modifier.size(Dimensions.IconSizeNormal)
                    )
                    LogStatus.MISSED -> Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = stringResource(R.string.content_description_missed),
                        tint = InactivoDeshabilitado,
                        modifier = Modifier.size(Dimensions.IconSizeNormal)
                    )
                    LogStatus.PARTIAL -> Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = stringResource(R.string.content_description_partial),
                        tint = AcentoInformativo,
                        modifier = Modifier.size(Dimensions.IconSizeNormal)
                    )
                    else -> Unit
                }

                // Menú de acciones
                Box {
                    IconButton(
                        onClick = { showMenu = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.more_options),
                            modifier = Modifier.size(Dimensions.IconSizeSmall)
                        )
                    }

                    HabitContextMenu(
                        expanded = showMenu,
                        onDismiss = { showMenu = false },
                        isArchived = isArchived,
                        isSkippedToday = isSkippedToday,
                        isCompletedToday = isCompletedToday,
                        canToggleSkipped = canToggleSkipped,
                        onArchiveHabit = {
                            showArchiveDialog = true
                            showMenu = false
                        },
                        onUnarchiveHabit = {
                            showArchiveDialog = true
                            showMenu = false
                        },
                        onMarkSkipped = {
                            onMarkSkipped?.invoke()
                            showMenu = false
                        },
                        onUndoSkipped = {
                            onUndoSkipped?.invoke()
                            showMenu = false
                        }
                    )
                }
            }

            // Barra de progreso (solo si no está saltado y hay progreso)
            if (!isSkippedToday && completionProgressPercentage > 0f) {
                Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
                LinearProgressIndicator(
                    progress = { (completionProgressPercentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.ProgressBarHeightLarge)
                        .clip(RoundedCornerShape(Dimensions.CornerRadiusSmall / 2)),
                    color = accentColor,
                    trackColor = InactivoDeshabilitado.copy(alpha = AlphaValues.DisabledAlpha)
                )
            }

            if (showArchiveDialog) {
                ConfirmationDialog(
                    onDismissRequest = { showArchiveDialog = false },
                    title = if (!isArchived) {
                        stringResource(R.string.title_archive_habit)
                    } else {
                        stringResource(R.string.title_unarchive_habit)
                    },
                    message = if (!isArchived) {
                        stringResource(R.string.action_archive_habit)
                    } else {
                        stringResource(R.string.action_unarchive_habit)
                    },
                    onConfirm = {
                        if (isArchived) onUnarchiveHabit?.invoke() else onArchiveHabit?.invoke()
                        showArchiveDialog = false
                    },
                    confirmText = if (!isArchived) {
                        stringResource(R.string.archive)
                    } else {
                        stringResource(R.string.unarchive)
                    },
                    cancelText = stringResource(R.string.cancel),
                    icon = Icons.Default.Archive
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
                                tint = if (canDecrementProgress)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaValues.DisabledContentAlpha)
                            )
                        }

                        // Contador
                        Surface(
                            color = InactivoDeshabilitado.copy(alpha = AlphaValues.DisabledAlpha),
                            shape = RoundedCornerShape(Dimensions.CornerRadius),
                            modifier = Modifier.padding(horizontal = Dimensions.SpacingSmall)
                        ) {
                            Text(
                                text = "$currentCompletionCount/$dailyTarget",
                                style = Typography.labelMedium,
                                modifier = Modifier.padding(
                                    horizontal = Dimensions.SpacingSmall,
                                    vertical = Dimensions.SpacingExtraSmall
                                ),
                                color = MaterialTheme.colorScheme.onSurface
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
                                tint = if (canIncrementProgress)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaValues.DisabledContentAlpha)
                            )
                        }
                    } else {
                        // Para hábitos simples (dailyTarget = null o 1)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.habit_card_complete),
                                style = Typography.bodyLarge,
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
                                enabled = !isArchived && !isSkippedToday,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = AcentoPositivo,
                                    uncheckedColor = InactivoDeshabilitado
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}