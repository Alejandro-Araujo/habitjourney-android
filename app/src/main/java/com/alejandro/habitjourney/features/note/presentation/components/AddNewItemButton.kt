package com.alejandro.habitjourney.features.note.presentation.components


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButton
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButtonType


@Composable
fun AddNewItemButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HabitJourneyButton(
        text = stringResource(R.string.add_list_item),
        onClick = onClick,
        type = HabitJourneyButtonType.TERTIARY,
        leadingIcon = Icons.Default.Add,
        modifier = modifier
    )
}