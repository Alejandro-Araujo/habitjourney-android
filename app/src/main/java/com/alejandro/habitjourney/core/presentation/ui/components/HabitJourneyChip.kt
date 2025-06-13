package com.alejandro.habitjourney.core.presentation.ui.components


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.core.presentation.ui.theme.*

enum class HabitJourneyChipType {
    FILTER,     // Para filtros
    INPUT,      // Para entrada/tags
    CHOICE,     // Para selección múltiple
    ACTION      // Para acciones rápidas
}

@Composable
fun HabitJourneyChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: HabitJourneyChipType = HabitJourneyChipType.FILTER,
    selected: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    chipColor: Color = AcentoInformativo,
    selectedColor: Color = AcentoInformativo,
    unselectedColor: Color = MaterialTheme.colorScheme.surface
) {
    val containerColor = when {
        !enabled -> InactivoDeshabilitado.copy(alpha = 0.12f)
        selected -> selectedColor
        else -> unselectedColor
    }

    val contentColor = when {
        !enabled -> InactivoDeshabilitado
        selected -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    val borderColor = when {
        !enabled -> InactivoDeshabilitado.copy(alpha = 0.12f)
        selected -> Color.Transparent
        else -> chipColor.copy(alpha = 0.5f)
    }

    Surface(
        onClick = onClick,
        modifier = modifier.height(32.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = text,
                style = Typography.labelMedium,
                color = contentColor
            )

            trailingIcon?.let {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
            }
        }
    }
}