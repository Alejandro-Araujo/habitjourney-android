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
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDateSelector(
    selectedDate: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    enabled: Boolean,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = Typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { if (enabled) showDatePicker = true },
                modifier = Modifier.weight(1f),
                enabled = enabled,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AcentoInformativo
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.IconSizeButton)
                )
                Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
                Text(
                    text = selectedDate?.let { TaskDateUtils.formatDateForDisplay(it) } ?: placeholder,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (selectedDate != null && enabled) {
                IconButton(
                    onClick = { onDateChange(null) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.clear_date),
                        tint = Error
                    )
                }
            }
        }
    }

    // DatePicker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        TaskDatePickerDialog(
            datePickerState = datePickerState,
            onDateSelected = { timestamp ->
                timestamp?.let {
                    val instant = Instant.fromEpochMilliseconds(it)
                    val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                    onDateChange(date)
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}