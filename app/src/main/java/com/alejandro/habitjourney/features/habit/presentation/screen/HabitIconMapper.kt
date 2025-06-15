package com.alejandro.habitjourney.features.habit.presentation.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import com.alejandro.habitjourney.core.data.local.enums.HabitType

/**
 * Un objeto de utilidad que mapea un [HabitType] a un [ImageVector] correspondiente.
 *
 * Se utiliza para obtener el icono visual apropiado para cada tipo de hábito en la UI,
 * centralizando la lógica de selección de iconos.
 */
object HabitIconMapper {
    /**
     * Devuelve el [ImageVector] asociado a un [HabitType] específico.
     *
     * @param habitType El tipo de hábito para el que se necesita un icono.
     * @return El [ImageVector] correspondiente.
     */
    fun getIconForHabitType(habitType: HabitType): ImageVector {
        return when (habitType) {
            // En el MVP actual, DO es el tipo principal.
            HabitType.DO -> Icons.Default.SelfImprovement
            HabitType.QUANTITATIVE -> Icons.Default.Numbers
            HabitType.TIMER -> Icons.Default.Timer
        }
    }
}
