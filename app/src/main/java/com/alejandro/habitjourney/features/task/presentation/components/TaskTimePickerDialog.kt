package com.alejandro.habitjourney.features.task.presentation.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButton
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButtonType
import com.alejandro.habitjourney.core.presentation.ui.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTimePickerDialog(
    timePickerState: TimePickerState,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.SpacingMedium),
            shape = RoundedCornerShape(Dimensions.CornerRadiusLarge),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.SpacingLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.select_time),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

                // TimePicker
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        selectorColor = AcentoInformativo,
                        periodSelectorSelectedContainerColor = AcentoInformativo
                    )
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
                ) {
                    HabitJourneyButton(
                        text = stringResource(R.string.cancel),
                        onClick = onDismiss,
                        type = HabitJourneyButtonType.SECONDARY,
                        modifier = Modifier.weight(1f)
                    )

                    HabitJourneyButton(
                        text = stringResource(R.string.ok),
                        onClick = {
                            onTimeSelected(timePickerState.hour, timePickerState.minute)
                        },
                        type = HabitJourneyButtonType.PRIMARY,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}