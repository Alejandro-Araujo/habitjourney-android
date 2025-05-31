package com.alejandro.habitjourney.features.task.presentation.components


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyTextField
import com.alejandro.habitjourney.core.presentation.ui.theme.*

@Composable
fun TaskTitleField(
    title: String,
    onTitleChange: (String) -> Unit,
    error: String?,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    HabitJourneyTextField(
        value = title,
        onValueChange = onTitleChange,
        label = stringResource(R.string.task_title),
        placeholder = stringResource(R.string.task_title_placeholder),
        modifier = modifier,
        enabled = enabled,
        isError = error != null,
        helperText = error,
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Assignment,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.IconSizeNormal)
            )
        }
    )
}