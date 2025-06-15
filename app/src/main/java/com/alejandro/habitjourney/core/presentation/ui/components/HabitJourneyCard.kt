package com.alejandro.habitjourney.core.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.core.presentation.ui.theme.*

enum class HabitJourneyCardType {
    ELEVATED,
    OUTLINED,
    FILLED,
    TRANSPARENT
}

/**
 * Card personalizada de HabitJourney con cuatro variantes visuales.
 * Base para todas las cards de la aplicación con estilos consistentes.
 *
 * @param onClick Callback opcional para hacer la card clickeable
 * @param containerColor Color de fondo de la card
 * @param cardType Variante visual (ELEVATED, OUTLINED, FILLED, TRANSPARENT)
 * @param content Contenido de la card usando ColumnScope
 */
@Composable
fun HabitJourneyCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    cardType: HabitJourneyCardType = HabitJourneyCardType.ELEVATED,
    borderColor: Color = InactivoDeshabilitado.copy(alpha = AlphaValues.CardStateAlpha),
    elevation: Dp = when (cardType) {
        HabitJourneyCardType.ELEVATED -> Dimensions.ElevationLevel1
        else -> 0.dp
    },
    content: @Composable ColumnScope.() -> Unit
) {
    val actualContainerColor = when (cardType) {
        HabitJourneyCardType.FILLED -> MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
        HabitJourneyCardType.TRANSPARENT -> Color.Transparent
        else -> containerColor
    }

    val border = when (cardType) {
        HabitJourneyCardType.OUTLINED -> BorderStroke(
            width = Dimensions.BorderWidth,
            color = borderColor
        )

        else -> null
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.CornerRadius))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(Dimensions.CornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = actualContainerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = border
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.SpacingMedium)
        ) {
            content()
        }
    }
}

/**
 * Card compacta optimizada para mostrar estadísticas.
 * Layout predefinido: título, valor principal y subtítulo opcional.
 *
 * @param title Etiqueta de la estadística
 * @param value Valor principal a destacar
 * @param subtitle Información adicional opcional
 * @param accentColor Color del valor principal
 */
@Composable
fun HabitJourneyStatsCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    accentColor: Color = AcentoInformativo,
    onClick: (() -> Unit)? = null
) {
    HabitJourneyCard(
        modifier = modifier,
        onClick = onClick,
        cardType = HabitJourneyCardType.FILLED
    ) {
        Text(
            text = title,
            style = Typography.labelMedium,
            color = InactivoDeshabilitado
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingExtraSmall))

        Text(
            text = value,
            style = Typography.headlineMedium,
            color = accentColor
        )

        subtitle?.let {
            Spacer(modifier = Modifier.height(Dimensions.SpacingExtraSmall / 2))
            Text(
                text = it,
                style = Typography.bodySmall,
                color = InactivoDeshabilitado
            )
        }
    }
}