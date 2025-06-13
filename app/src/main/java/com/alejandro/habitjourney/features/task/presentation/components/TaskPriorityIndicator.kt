package com.alejandro.habitjourney.features.task.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.alejandro.habitjourney.core.data.local.enums.Priority
import com.alejandro.habitjourney.core.presentation.ui.theme.*

@Composable
fun TaskPriorityIndicator(
    priority: Priority,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(Dimensions.IconSizeSmall)
            .clip(CircleShape)
            .background(
                when (priority) {
                    Priority.HIGH -> AcentoUrgente
                    Priority.MEDIUM -> Logro
                    Priority.LOW -> AcentoPositivo
                }
            )
    )
}