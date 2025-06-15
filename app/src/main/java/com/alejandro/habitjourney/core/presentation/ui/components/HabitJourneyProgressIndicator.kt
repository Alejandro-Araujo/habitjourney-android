package com.alejandro.habitjourney.core.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoInformativo
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoPositivo
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions
import kotlin.math.roundToInt

enum class HabitJourneyProgressType {
    LINEAR,
    CIRCULAR,
    MINI
}

/**
 * Indicador de progreso con tres variantes visuales y animación suave.
 * Soporta labels personalizados y colores temáticos.
 *
 * @param progress Valor entre 0.0 y 1.0
 * @param type Variante visual (LINEAR, CIRCULAR, MINI)
 * @param showLabel Si mostrar etiqueta de porcentaje
 * @param label Texto personalizado (opcional)
 * @param progressColor Color de la barra de progreso
 * @param animationDuration Duración de animación en milisegundos
 */
@Composable
fun HabitJourneyProgressIndicator(
    progress: Float, // Valor entre 0.0 y 1.0
    modifier: Modifier = Modifier,
    type: HabitJourneyProgressType = HabitJourneyProgressType.LINEAR,
    showLabel: Boolean = true,
    label: String? = null,
    progressColor: Color = AcentoPositivo,
    trackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
    animationDuration: Int = 1000,
    size: Dp = 48.dp
) {
    // Animar el progreso
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = animationDuration),
        label = "progress_animation"
    )

    when (type) {
        HabitJourneyProgressType.LINEAR -> {
            LinearProgressContent(
                progress = animatedProgress,
                modifier = modifier,
                showLabel = showLabel,
                label = label,
                progressColor = progressColor,
                trackColor = trackColor
            )
        }
        HabitJourneyProgressType.CIRCULAR -> {
            CircularProgressContent(
                progress = animatedProgress,
                modifier = modifier,
                showLabel = showLabel,
                label = label,
                progressColor = progressColor,
                trackColor = trackColor,
                size = size
            )
        }
        HabitJourneyProgressType.MINI -> {
            MiniProgressContent(
                progress = animatedProgress,
                modifier = modifier,
                progressColor = progressColor,
                trackColor = trackColor
            )
        }
    }
}

@Composable
private fun LinearProgressContent(
    progress: Float,
    modifier: Modifier,
    showLabel: Boolean,
    label: String?,
    progressColor: Color,
    trackColor: Color
) {
    Column(modifier = modifier) {
        if (showLabel) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label ?: stringResource(R.string.progress_default_label), // Usa stringResource
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.progress_percentage_format, (progress * 100).roundToInt()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
        }

        LinearProgressIndicator(
            progress = {
                progress
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(Dimensions.CornerRadius)),
            color = progressColor,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round,
        )
    }
}

@Composable
private fun CircularProgressContent(
    progress: Float,
    modifier: Modifier,
    showLabel: Boolean,
    label: String?,
    progressColor: Color,
    trackColor: Color,
    size: Dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(size)
        ) {
            val strokeWidth = 4.dp.toPx()
            val radius = (size.toPx() - strokeWidth) / 2f

            drawCircle(
                color = trackColor,
                radius = radius,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        if (showLabel) {
            Text(
                text = label ?: stringResource(R.string.progress_percentage_format, (progress * 100).roundToInt()), // Usa stringResource
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MiniProgressContent(
    progress: Float,
    modifier: Modifier,
    progressColor: Color,
    trackColor: Color
) {
    LinearProgressIndicator(
        progress = {
            progress
        },
        modifier = modifier
            .width(40.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp)),
        color = progressColor,
        trackColor = trackColor,
        strokeCap = StrokeCap.Round,
    )
}
