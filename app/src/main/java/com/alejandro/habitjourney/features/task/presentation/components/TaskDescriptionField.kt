package com.alejandro.habitjourney.features.task.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyTextField
import com.alejandro.habitjourney.core.presentation.ui.theme.*

@Composable
fun TaskDescriptionField(
    description: String,
    onDescriptionChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    HabitJourneyTextField(
        value = description,
        onValueChange = onDescriptionChange,
        label = stringResource(R.string.task_description),
        placeholder = stringResource(R.string.task_description_placeholder),
        modifier = modifier,
        enabled = enabled,
        singleLine = false,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.IconSizeNormal)
            )
        }
    )
}