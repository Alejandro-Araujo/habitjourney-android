package com.alejandro.habitjourney.features.task.presentation.components

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import kotlinx.datetime.*
import com.alejandro.habitjourney.core.utils.formatter.DateTimeFormatters


/**
 * Un componente Composable que permite al usuario alternar la activación de un recordatorio para una tarea
 * y seleccionar la fecha y hora del mismo.
 *
 * Ofrece un interruptor para habilitar/deshabilitar el recordatorio y, si está habilitado,
 * una interfaz para seleccionar una fecha y hora, junto con chips de sugerencias rápidas.
 *
 * @param modifier Modificador para aplicar a este composable.
 * @param isReminderEnabled El estado actual del interruptor del recordatorio (habilitado o deshabilitado).
 * @param reminderDateTime La fecha y hora actual seleccionada para el recordatorio, o `null` si no hay ninguna.
 * @param onReminderEnabledChange Lambda que se invoca cuando el estado del interruptor cambia.
 * @param onReminderDateTimeChange Lambda que se invoca cuando la fecha y hora del recordatorio cambian.
 * @param enabled Controla si el componente está habilitado para la interacción del usuario.
 * @param context El contexto de la aplicación, necesario para acceder a recursos y formatear fechas.
 */
@Composable
fun TaskReminderToggle(
    modifier: Modifier = Modifier,
    isReminderEnabled: Boolean,
    reminderDateTime: LocalDateTime?,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderDateTimeChange: (LocalDateTime?) -> Unit,
    enabled: Boolean = true,
    context: Context = LocalContext.current
) {
    var showDateTimePicker by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isReminderEnabled) {
                AcentoInformativo.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (isReminderEnabled) {
            CardDefaults.outlinedCardBorder().copy(
                width = 2.dp,
                brush = androidx.compose.ui.graphics.SolidColor(AcentoInformativo)
            )
        } else null
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.SpacingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = if (isReminderEnabled) AcentoInformativo else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(Dimensions.IconSizeNormal)
                )

                Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.reminder_label),
                        style = Typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(R.string.reminder_description),
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = isReminderEnabled,
                    onCheckedChange = onReminderEnabledChange,
                    enabled = enabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = AcentoInformativo,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            AnimatedVisibility(
                visible = isReminderEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = Dimensions.SpacingSmall),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Dimensions.CornerRadius))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable(enabled = enabled) { showDateTimePicker = true }
                            .padding(Dimensions.SpacingSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = AcentoInformativo,
                            modifier = Modifier.size(Dimensions.IconSizeSmall)
                        )

                        Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (reminderDateTime != null) {
                                    formatReminderDateTime(reminderDateTime, context)
                                } else {
                                    stringResource(R.string.select_reminder_time)
                                },
                                style = Typography.bodyMedium,
                                color = if (reminderDateTime != null) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )

                            if (reminderDateTime != null) {
                                Text(
                                    text = getRelativeTimeString(reminderDateTime, context),
                                    style = Typography.bodySmall,
                                    color = AcentoInformativo
                                )
                            }
                        }

                        if (reminderDateTime != null) {
                            IconButton(
                                onClick = { onReminderDateTimeChange(null) },
                                enabled = enabled
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.clear_reminder),
                                    tint = Error,
                                    modifier = Modifier.size(Dimensions.IconSizeSmall)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(Dimensions.IconSizeSmall)
                            )
                        }
                    }

                    if (reminderDateTime == null) {
                        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
                        ) {
                            QuickReminderChip(
                                text = stringResource(R.string.in_1_hour),
                                onClick = {
                                    val timeZone = TimeZone.currentSystemDefault()
                                    val nowAsInstant = Clock.System.now()
                                    val oneHourLaterAsInstant = nowAsInstant.plus(DateTimePeriod(hours = 1), timeZone)
                                    onReminderDateTimeChange(oneHourLaterAsInstant.toLocalDateTime(timeZone))
                                },
                                enabled = enabled
                            )

                            QuickReminderChip(
                                text = stringResource(R.string.tomorrow_9am),
                                onClick = {
                                    val tomorrow = Clock.System.now()
                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                        .date
                                        .plus(1, DateTimeUnit.DAY)
                                    onReminderDateTimeChange(
                                        LocalDateTime(tomorrow, LocalTime(9, 0))
                                    )
                                },
                                enabled = enabled
                            )

                            QuickReminderChip(
                                text = stringResource(R.string.next_week),
                                onClick = {
                                    val nextWeek = Clock.System.now()
                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                        .date
                                        .plus(7, DateTimeUnit.DAY)
                                    onReminderDateTimeChange(
                                        LocalDateTime(nextWeek, LocalTime(9, 0))
                                    )
                                },
                                enabled = enabled
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDateTimePicker) {
        val initialDateTime = reminderDateTime ?: run {
            val timeZone = TimeZone.currentSystemDefault()
            val nowAsInstant = Clock.System.now()
            val oneHourLaterAsInstant = nowAsInstant.plus(DateTimePeriod(hours = 1), timeZone)
            oneHourLaterAsInstant.toLocalDateTime(timeZone)
        }

        TaskDateTimePickerDialog(
            onDateTimeSelected = { dateTime ->
                onReminderDateTimeChange(dateTime)
                showDateTimePicker = false
            },
            onDismiss = { showDateTimePicker = false },
            initialDateTime = initialDateTime
        )
    }
}

/**
 * Un chip de acción rápida para seleccionar un recordatorio predefinido.
 *
 * @param text El texto a mostrar en el chip.
 * @param onClick Lambda que se invoca cuando se hace clic en el chip.
 * @param enabled Si `false`, el chip está deshabilitado para la interacción.
 */
@Composable
private fun QuickReminderChip(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = text,
                style = Typography.bodySmall
            )
        },
        enabled = enabled,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        border = null
    )
}

/**
 * Formatea una [LocalDateTime] para mostrarla de forma legible.
 * Utiliza los formateadores definidos en [DateTimeFormatters].
 *
 * @param dateTime La [LocalDateTime] a formatear.
 * @param context El contexto para obtener los recursos de string y formatear fechas.
 * @return Un [String] con la fecha y hora formateadas.
 */
@SuppressLint("DefaultLocale")
@Composable
private fun formatReminderDateTime(dateTime: LocalDateTime, context: Context): String {
    return DateTimeFormatters.formatDateTimeLocalized(dateTime)
}

/**
 * Calcula y devuelve una cadena de tiempo relativo para una [LocalDateTime] dada.
 * Por ejemplo, "en 5 minutos", "en 2 horas", "en 3 días".
 *
 * @param dateTime La [LocalDateTime] objetivo para la cual calcular el tiempo relativo.
 * @param context El contexto para obtener los strings localizados.
 * @return Un [String] que representa el tiempo restante de forma relativa.
 */
@Composable
private fun getRelativeTimeString(dateTime: LocalDateTime, context: Context): String {
    val now = Clock.System.now()
    val target = dateTime.toInstant(TimeZone.currentSystemDefault())
    val duration = target - now

    return when {
        duration.inWholeMinutes < 60 -> {
            context.getString(R.string.in_x_minutes, duration.inWholeMinutes.toInt())
        }
        duration.inWholeHours < 24 -> {
            context.getString(R.string.in_x_hours, duration.inWholeHours.toInt())
        }
        duration.inWholeDays < 7 -> {
            context.getString(R.string.in_x_days, duration.inWholeDays.toInt())
        }
        else -> {
            context.getString(R.string.in_x_weeks, (duration.inWholeDays / 7).toInt())
        }
    }
}