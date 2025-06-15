package com.alejandro.habitjourney.features.habit.presentation.viewmodel

import com.alejandro.habitjourney.features.habit.domain.model.Habit
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.alejandro.habitjourney.core.data.local.enums.HabitType
import com.alejandro.habitjourney.features.habit.domain.model.HabitLog
import com.alejandro.habitjourney.core.data.local.enums.LogStatus

/**
 * Modelo UI para representar un hábito en la lista.
 * Contiene toda la información necesaria para mostrar un hábito y su estado actual de progreso en la UI.
 */
@Immutable
data class HabitListItemUiModel(
    val id: Long,
    val name: String,
    val description: String?,
    val type: HabitType,
    val icon: ImageVector,
    val dailyTarget: Int?,
    val currentCompletionCount: Int,
    val completionProgressPercentage: Float,
    val logStatus: LogStatus,
    val isArchived: Boolean,

    // Propiedades de conveniencia para la UI basadas en logStatus
    val isCompletedToday: Boolean,
    val isSkippedToday: Boolean,
    val isPartialToday: Boolean,
    val isMissedToday: Boolean,
    val isNotCompletedToday: Boolean
) {
    val canIncrementProgress: Boolean
        get() = !isArchived && logStatus != LogStatus.SKIPPED &&
                (dailyTarget == null || currentCompletionCount < dailyTarget)

    val canDecrementProgress: Boolean
        get() = !isArchived && currentCompletionCount > 0 && logStatus != LogStatus.SKIPPED

    val canToggleSkipped: Boolean
        get() = !isArchived && logStatus != LogStatus.COMPLETED
}

/**
 * Función de extensión para convertir Habit a HabitListItemUiModel.
 *
 * @param currentCompletionCount Conteo actual de veces que se ha completado el hábito hoy (valor numérico).
 * @param todayLog El log específico del día actual para este hábito. Puede ser null.
 * @param icon El ImageVector asociado al tipo de hábito.
 */
fun Habit.toHabitListItemUiModel(
    currentCompletionCount: Int = 0,
    todayLog: HabitLog?,
    icon: ImageVector
): HabitListItemUiModel {
    val progressPercentage = when {
        dailyTarget == null || dailyTarget <= 0 -> {
            if (currentCompletionCount > 0) 100f else 0f
        }
        dailyTarget == 1 -> {
            if (currentCompletionCount >= 1) 100f else 0f
        }
        else -> {
            (currentCompletionCount.toFloat() / dailyTarget.toFloat() * 100f).coerceAtMost(100f)
        }
    }

    val effectiveLogStatus = when {
        todayLog?.status == LogStatus.SKIPPED -> LogStatus.SKIPPED
        todayLog?.status == LogStatus.MISSED -> LogStatus.MISSED
        dailyTarget == null || dailyTarget <= 1 -> {
            if (currentCompletionCount >= 1) LogStatus.COMPLETED else LogStatus.NOT_COMPLETED
        }
        else -> {
            when {
                currentCompletionCount >= dailyTarget -> LogStatus.COMPLETED
                currentCompletionCount > 0 -> LogStatus.PARTIAL
                else -> LogStatus.NOT_COMPLETED
            }
        }
    }

    return HabitListItemUiModel(
        id = id,
        name = name,
        description = description,
        type = type,
        icon = icon,
        dailyTarget = dailyTarget,
        currentCompletionCount = currentCompletionCount,
        completionProgressPercentage = progressPercentage,
        logStatus = effectiveLogStatus,
        isArchived = this.isArchived,
        isCompletedToday = effectiveLogStatus == LogStatus.COMPLETED,
        isSkippedToday = effectiveLogStatus == LogStatus.SKIPPED,
        isPartialToday = effectiveLogStatus == LogStatus.PARTIAL,
        isMissedToday = effectiveLogStatus == LogStatus.MISSED,
        isNotCompletedToday = effectiveLogStatus == LogStatus.NOT_COMPLETED
    )
}