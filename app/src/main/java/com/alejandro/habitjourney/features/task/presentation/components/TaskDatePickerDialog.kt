package com.alejandro.habitjourney.features.task.presentation.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButton
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButtonType
import com.alejandro.habitjourney.core.presentation.ui.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDatePickerDialog(
    datePickerState: DatePickerState,
    onDateSelected: (Long?) -> Unit,
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
                .fillMaxWidth(0.98f)
                .wrapContentHeight()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            shape = RoundedCornerShape(Dimensions.CornerRadiusLarge),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding( top = Dimensions.SpacingLarge,
                    bottom = Dimensions.SpacingLarge,
                    start = 12.dp,
                    end = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.select_date),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // DatePicker
                    DatePicker(
                        state = datePickerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 2.dp, end = 6.dp),
                        showModeToggle = true,
                        colors = DatePickerDefaults.colors(
                            selectedDayContainerColor = AcentoInformativo,
                            todayDateBorderColor = AcentoInformativo,
                            dayContentColor = MaterialTheme.colorScheme.onSurface,
                            selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                            todayContentColor = AcentoInformativo
                        )
                    )
                }

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
                            onDateSelected(datePickerState.selectedDateMillis)
                        },
                        type = HabitJourneyButtonType.PRIMARY,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}