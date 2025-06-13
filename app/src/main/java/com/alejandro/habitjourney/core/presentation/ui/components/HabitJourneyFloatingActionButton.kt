package com.alejandro.habitjourney.core.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoInformativo
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoPositivo
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoUrgente
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions
import com.alejandro.habitjourney.core.presentation.ui.theme.HabitJourneyTheme

enum class HabitJourneyFabType {
    REGULAR,    // FAB normal
    SMALL,      // FAB pequeño
    EXTENDED,   // FAB extendido con texto
    LARGE       // FAB grande personalizado
}

@Composable
fun HabitJourneyFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: HabitJourneyFabType = HabitJourneyFabType.REGULAR,
    icon: ImageVector = Icons.Default.Add,
    text: String? = null, // Solo usado en EXTENDED
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
               // shape = RoundedCornerShape(size / 2)
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

// Composables de conveniencia para casos específicos
@Composable
fun AddHabitFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false
) {
    if (isExpanded) {
        HabitJourneyFloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            type = HabitJourneyFabType.EXTENDED,
            icon = Icons.Default.Add,
            text = stringResource(R.string.fab_add_habit_text),
            containerColor = AcentoPositivo,
            iconContentDescription = stringResource(R.string.content_description_add_habit_fab)
        )
    } else {
        HabitJourneyFloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            icon = Icons.Default.Add,
            containerColor = AcentoPositivo,
            iconContentDescription = stringResource(R.string.content_description_add_habit_fab)
        )
    }
}

@Composable
fun QuickActionFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isOpen: Boolean = false
) {
    HabitJourneyFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        icon = if (isOpen) Icons.Default.Close else Icons.Default.Add,
        isRotated = isOpen,
        containerColor = AcentoInformativo,
        iconContentDescription = if (isOpen) stringResource(R.string.content_description_close_quick_action_fab) else stringResource(R.string.content_description_open_quick_action_fab)
    )
}

@Composable
fun StartHabitFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HabitJourneyFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        type = HabitJourneyFabType.SMALL,
        icon = Icons.Default.PlayArrow,
        containerColor = AcentoPositivo,
        iconContentDescription = stringResource(R.string.content_description_start_habit_fab)
    )
}

@Composable
fun EditFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HabitJourneyFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        type = HabitJourneyFabType.SMALL,
        icon = Icons.Default.Create,
        containerColor = MaterialTheme.colorScheme.secondary,
        iconContentDescription = stringResource(R.string.content_description_edit_fab)
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewRegularFab() {
    HabitJourneyTheme {
        Column(
            modifier = Modifier.padding(Dimensions.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HabitJourneyFloatingActionButton(
                onClick = { /* Action */ },
                type = HabitJourneyFabType.REGULAR,
                iconContentDescription = stringResource(R.string.content_description_add_default_fab)
            )

            HabitJourneyFloatingActionButton(
                onClick = { /* Action */ },
                type = HabitJourneyFabType.SMALL,
                containerColor = AcentoPositivo,
                iconContentDescription = stringResource(R.string.content_description_add_small_fab)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewExtendedFab() {
    HabitJourneyTheme {
        Column(
            modifier = Modifier.padding(Dimensions.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HabitJourneyFloatingActionButton(
                onClick = { /* Action */ },
                type = HabitJourneyFabType.EXTENDED,
                text = stringResource(R.string.fab_add_habit_text),
                containerColor = AcentoPositivo,
                iconContentDescription = stringResource(R.string.content_description_add_habit_fab)
            )

            HabitJourneyFloatingActionButton(
                onClick = { /* Action */ },
                type = HabitJourneyFabType.EXTENDED,
                icon = Icons.Default.Create,
                text = stringResource(R.string.fab_edit_text),
                containerColor = MaterialTheme.colorScheme.secondary,
                iconContentDescription = stringResource(R.string.content_description_edit_fab)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLargeFab() {
    HabitJourneyTheme {
        HabitJourneyFloatingActionButton(
            onClick = { /* Action */ },
            type = HabitJourneyFabType.LARGE,
            // ¡Usamos la nueva dimensión aquí!
            size = Dimensions.FabSizeLarge,
            containerColor = AcentoInformativo,
            iconContentDescription = stringResource(R.string.content_description_add_large_fab)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSpecializedFabs() {
    HabitJourneyTheme {
        Column(
            modifier = Modifier.padding(Dimensions.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.specialized_fabs_title),
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
            ) {
                AddHabitFab(onClick = { /* Action */ })
                QuickActionFab(onClick = { /* Action */ }, isOpen = false)
                QuickActionFab(onClick = { /* Action */ }, isOpen = true)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
            ) {
                StartHabitFab(onClick = { /* Action */ })
                EditFab(onClick = { /* Action */ })
            }

            AddHabitFab(
                onClick = { /* Action */ },
                isExpanded = true
            )
        }
    }
}