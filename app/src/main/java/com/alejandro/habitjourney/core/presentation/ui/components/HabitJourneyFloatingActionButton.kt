package com.alejandro.habitjourney.core.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoInformativo
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions

enum class HabitJourneyFabType {
    REGULAR,
    SMALL,
    EXTENDED,
    LARGE
}

/**
 * FAB personalizado con cuatro tamaños y animación de rotación configurable.
 * Soporta iconos dinámicos y texto para variante extendida.
 *
 * @param onClick Callback cuando se presiona
 * @param type Variante de tamaño (REGULAR, SMALL, EXTENDED, LARGE)
 * @param icon Icono a mostrar
 * @param text Texto para variante EXTENDED
 * @param isRotated Si debe rotar el icono (útil para transiciones +/×)
 * @param animateRotation Si debe animar la rotación
 */
@Composable
fun HabitJourneyFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: HabitJourneyFabType = HabitJourneyFabType.REGULAR,
    icon: ImageVector = Icons.Default.Add,
    text: String? = null,
    containerColor: Color = AcentoInformativo,
    contentColor: Color = Color.White,
    size: Dp = Dimensions.FabSize,
    elevation: Dp = Dimensions.ElevationLevel2,
    isRotated: Boolean = false,
    animateRotation: Boolean = true,
    iconContentDescription: String? = null,
    isLoading: Boolean = false
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isRotated) 45f else 0f,
        animationSpec = tween(durationMillis = if (animateRotation) 300 else 0),
        label = "fab_rotation"
    )

    val defaultIconContentDescription = when (icon) {
        Icons.Default.Add -> stringResource(R.string.content_description_add_icon)
        Icons.Default.Close -> stringResource(R.string.content_description_close_icon)
        Icons.Default.Create -> stringResource(R.string.content_description_edit_icon)
        Icons.Default.PlayArrow -> stringResource(R.string.content_description_play_icon)
        else -> null
    }

    when (type) {
        HabitJourneyFabType.REGULAR -> {
            FloatingActionButton(
                onClick = onClick,
                modifier = modifier.size(size),
                containerColor = containerColor,
                contentColor = contentColor,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = elevation
                ),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconContentDescription ?: defaultIconContentDescription,
                    modifier = if (animateRotation) {
                        Modifier.rotate(rotationAngle)
                    } else Modifier
                )
            }
        }

        HabitJourneyFabType.SMALL -> {
            SmallFloatingActionButton(
                onClick = onClick,
                modifier = modifier,
                containerColor = containerColor,
                contentColor = contentColor,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = elevation
                )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconContentDescription ?: defaultIconContentDescription,
                    modifier = if (animateRotation) {
                        Modifier.rotate(rotationAngle)
                    } else Modifier
                )
            }
        }

        HabitJourneyFabType.EXTENDED -> {
            ExtendedFloatingActionButton(
                onClick = onClick,
                modifier = modifier,
                containerColor = containerColor,
                contentColor = contentColor,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = elevation
                ),
                shape = RoundedCornerShape(Dimensions.CornerRadiusLarge)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconContentDescription ?: defaultIconContentDescription,
                    modifier = if (animateRotation) {
                        Modifier.rotate(rotationAngle)
                    } else Modifier
                )
                if (text != null) {
                    Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        HabitJourneyFabType.LARGE -> {
            FloatingActionButton(
                onClick = onClick,
                modifier = modifier.size(size),
                containerColor = containerColor,
                contentColor = contentColor,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = elevation
                ),
                shape = RoundedCornerShape(size / 2)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconContentDescription ?: defaultIconContentDescription,
                    modifier = Modifier
                        .size(Dimensions.IconSizeLarge)
                        .then(
                            if (animateRotation) {
                                Modifier.rotate(rotationAngle)
                            } else Modifier
                        )
                )
            }
        }
    }
}
