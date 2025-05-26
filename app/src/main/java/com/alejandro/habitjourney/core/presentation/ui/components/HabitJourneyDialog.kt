package com.alejandro.habitjourney.core.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoInformativo
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoUrgente
import com.alejandro.habitjourney.core.presentation.ui.theme.Error
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions
import com.alejandro.habitjourney.core.presentation.ui.theme.HabitJourneyTheme
import com.alejandro.habitjourney.core.presentation.ui.theme.Exito

enum class HabitJourneyDialogType {
    INFO,           // Información general
    WARNING,        // Advertencias
    ERROR,          // Errores
    SUCCESS,        // Confirmaciones exitosas
    CONFIRMATION    // Confirmación de acciones
}

@Composable
fun HabitJourneyDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    dialogType: HabitJourneyDialogType = HabitJourneyDialogType.INFO,
    icon: ImageVector? = null,
    confirmButtonText: String = stringResource(R.string.dialog_ok_button_text),
    dismissButtonText: String? = null,
    onConfirm: () -> Unit = onDismissRequest,
    onDismiss: (() -> Unit)? = null,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    content: @Composable (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.SpacingMedium),
            shape = RoundedCornerShape(Dimensions.CornerRadiusLarge),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = Dimensions.ElevationLevel3
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.SpacingLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono si se proporciona
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = when (dialogType) {
                            HabitJourneyDialogType.INFO -> stringResource(R.string.content_description_info_icon)
                            HabitJourneyDialogType.WARNING -> stringResource(R.string.content_description_warning_icon)
                            HabitJourneyDialogType.ERROR -> stringResource(R.string.content_description_error_icon)
                            HabitJourneyDialogType.SUCCESS -> stringResource(R.string.content_description_success_icon)
                            HabitJourneyDialogType.CONFIRMATION -> stringResource(R.string.content_description_confirmation_icon)
                        },
                        tint = getDialogColor(dialogType),
                        modifier = Modifier.size(Dimensions.IconSizeLarge)
                    )
                    Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
                }

                // Título
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingSmall + Dimensions.SpacingSmall / 2))

                // Mensaje
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                // Contenido personalizado (si se proporciona)
                if (content != null) {
                    Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
                    content()
                }

                Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (dismissButtonText != null) {
                        Arrangement.spacedBy(Dimensions.SpacingSmall + Dimensions.SpacingSmall / 2)
                    } else {
                        Arrangement.Center
                    }
                ) {
                    // Botón de cancelar/dismissal (si existe)
                    if (dismissButtonText != null) {
                        HabitJourneyButton(
                            text = dismissButtonText,
                            onClick = onDismiss ?: onDismissRequest,
                            type = HabitJourneyButtonType.SECONDARY,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Botón de confirmación
                    HabitJourneyButton(
                        text = confirmButtonText,
                        onClick = onConfirm,
                        type = when (dialogType) {
                            HabitJourneyDialogType.ERROR,
                            HabitJourneyDialogType.WARNING -> HabitJourneyButtonType.SECONDARY
                            else -> HabitJourneyButtonType.PRIMARY
                        },
                        modifier = if (dismissButtonText != null) {
                            Modifier.weight(1f)
                        } else {
                            Modifier.fillMaxWidth()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun getDialogColor(type: HabitJourneyDialogType): Color = when (type) {
    HabitJourneyDialogType.INFO -> AcentoInformativo
    HabitJourneyDialogType.WARNING -> AcentoUrgente
    HabitJourneyDialogType.ERROR -> Error
    HabitJourneyDialogType.SUCCESS -> Exito
    HabitJourneyDialogType.CONFIRMATION -> AcentoUrgente
}

// Composables de conveniencia para casos comunes
@Composable
fun ConfirmationDialog(
    onDismissRequest: () -> Unit,
    title: String = stringResource(R.string.dialog_confirmation_title),
    message: String,
    onConfirm: () -> Unit,
    confirmText: String = stringResource(R.string.dialog_confirm_button_text),
    cancelText: String = stringResource(R.string.dialog_cancel_button_text),
    icon: ImageVector = Icons.AutoMirrored.Filled.HelpOutline
) {
    HabitJourneyDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        message = message,
        dialogType = HabitJourneyDialogType.CONFIRMATION,
        icon = icon,
        confirmButtonText = confirmText,
        dismissButtonText = cancelText,
        onConfirm = onConfirm
    )
}

@Composable
fun ErrorDialog(
    onDismissRequest: () -> Unit,
    title: String = stringResource(R.string.dialog_error_title),
    message: String,
    icon: ImageVector = Icons.Default.Clear
) {
    HabitJourneyDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        message = message,
        dialogType = HabitJourneyDialogType.ERROR,
        icon = icon,
        confirmButtonText = stringResource(R.string.dialog_understood_button_text) // Usa stringResource
    )
}

@Composable
fun SuccessDialog(
    onDismissRequest: () -> Unit,
    title: String = stringResource(R.string.dialog_success_title),
    message: String,
    icon: ImageVector = Icons.Default.CheckCircle
) {
    HabitJourneyDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        message = message,
        dialogType = HabitJourneyDialogType.SUCCESS,
        icon = icon,
        confirmButtonText = stringResource(R.string.dialog_great_button_text) // Usa stringResource
    )
}

// --- PREVIEWS ---
@Preview(showBackground = true)
@Composable
fun PreviewInfoDialog() {
    HabitJourneyTheme {
        HabitJourneyDialog(
            onDismissRequest = { /* Dismiss */ },
            title = stringResource(R.string.dialog_info_title),
            message = stringResource(R.string.dialog_info_message),
            dialogType = HabitJourneyDialogType.INFO,
            icon = Icons.Default.Info,
            confirmButtonText = stringResource(R.string.dialog_understood_button_text)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewConfirmationDialog() {
    HabitJourneyTheme {
        ConfirmationDialog(
            onDismissRequest = { /* Dismiss */ },
            title = stringResource(R.string.dialog_confirmation_title),
            message = stringResource(R.string.dialog_delete_confirmation_message),
            onConfirm = { /* Confirm action */ },
            icon = Icons.Default.Warning
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewErrorDialog() {
    HabitJourneyTheme {
        ErrorDialog(
            onDismissRequest = { /* Dismiss */ },
            message = stringResource(R.string.dialog_error_default_message)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSuccessDialog() {
    HabitJourneyTheme {
        SuccessDialog(
            onDismissRequest = { /* Dismiss */ },
            message = stringResource(R.string.dialog_success_default_message)
        )
    }
}