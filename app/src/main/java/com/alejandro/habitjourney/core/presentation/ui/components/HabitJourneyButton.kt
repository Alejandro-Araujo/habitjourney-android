package com.alejandro.habitjourney.core.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.alejandro.habitjourney.R // Asegúrate de que esta importación sea correcta para tu proyecto
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoInformativo
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions

enum class HabitJourneyButtonType {
    PRIMARY, SECONDARY, TERTIARY
}

@Composable
fun HabitJourneyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: HabitJourneyButtonType = HabitJourneyButtonType.PRIMARY,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    iconContentDescription: String? = null
) {
    val buttonContent: @Composable () -> Unit = {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimensions.IconSizeNormal),
                    color = when (type) {
                        HabitJourneyButtonType.PRIMARY -> Color.White
                        else -> AcentoInformativo
                    },
                    strokeWidth = Dimensions.ProgressIndicatorStrokeWidth
                )
                Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
            } else if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = iconContentDescription ?: stringResource(R.string.content_description_leading_icon), // Usa el nuevo parámetro o el string por defecto
                    modifier = Modifier.size(Dimensions.IconSizeButton)
                )
                Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
            }

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )

            if (trailingIcon != null && !isLoading) {
                Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = iconContentDescription ?: stringResource(R.string.content_description_trailing_icon), // Usa el nuevo parámetro o el string por defecto
                    modifier = Modifier.size(Dimensions.IconSizeButton)
                )
            }
        }
    }

    when (type) {
        HabitJourneyButtonType.PRIMARY -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .fillMaxWidth()
                    .height(Dimensions.ButtonHeight),
                enabled = enabled && !isLoading,
                shape = RoundedCornerShape(Dimensions.CornerRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AcentoInformativo,
                    contentColor = Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                buttonContent()
            }
        }
        HabitJourneyButtonType.SECONDARY -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier
                    .fillMaxWidth()
                    .height(Dimensions.ButtonHeight),
                enabled = enabled && !isLoading,
                shape = RoundedCornerShape(Dimensions.CornerRadius),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AcentoInformativo,
                    containerColor = Color.Transparent,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                ),
                border = BorderStroke(
                    width = Dimensions.BorderWidth,
                    color = if (enabled && !isLoading) AcentoInformativo
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            ) {
                buttonContent()
            }
        }
        HabitJourneyButtonType.TERTIARY -> {
            TextButton(
                onClick = onClick,
                modifier = modifier.height(Dimensions.ButtonHeightTertiary),
                enabled = enabled && !isLoading,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = AcentoInformativo,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                buttonContent()
            }
        }
    }
}