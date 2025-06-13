package com.alejandro.habitjourney.features.habit.presentation.screen

import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.ui.graphics.vector.ImageVector
import com.alejandro.habitjourney.core.data.local.enums.HabitType

object HabitIconMapper {
    fun getIconForHabitType(habitType: HabitType): ImageVector {
        return when (habitType) {
            HabitType.DO -> Icons.Default.SelfImprovement // --> EN MVP SOLO TENDREMOS ESTE
            HabitType.QUANTITATIVE -> Icons.Default.Numbers //
            HabitType.TIMER -> Icons.Default.Timer //
            else -> Icons.Default.AccessibilityNew //
        }
    }
}