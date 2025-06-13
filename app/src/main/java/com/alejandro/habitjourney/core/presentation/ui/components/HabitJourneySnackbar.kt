package com.alejandro.habitjourney.core.presentation.ui.components


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.core.presentation.ui.theme.*

enum class SnackbarType {
    SUCCESS, ERROR, INFO, WARNING
}


@Composable
fun HabitJourneySnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { snackbarData ->
            HabitJourneySnackbar(
                snackbarData = snackbarData
            )
        }
    )
}

@Composable
private fun HabitJourneySnackbar(
    snackbarData: SnackbarData
) {
    // Determinar el tipo basado en el action label
    val type = when (snackbarData.visuals.actionLabel) {
        "SUCCESS" -> SnackbarType.SUCCESS
        "ERROR" -> SnackbarType.ERROR
        "WARNING" -> SnackbarType.WARNING
        else -> SnackbarType.INFO
    }

    val backgroundColor = when (type) {
        SnackbarType.SUCCESS -> AcentoPositivo
        SnackbarType.ERROR -> Error
        SnackbarType.WARNING -> AcentoUrgente
        SnackbarType.INFO -> AcentoInformativo
    }

    val icon = when (type) {
        SnackbarType.SUCCESS -> Icons.Default.CheckCircle
        SnackbarType.ERROR -> Icons.Default.Error
        SnackbarType.WARNING -> Icons.Default.Warning
        SnackbarType.INFO -> Icons.Default.Info
    }

    Snackbar(
        modifier = Modifier.padding(12.dp),
        containerColor = backgroundColor,
        contentColor = Color.White,
        actionContentColor = Color.White
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = snackbarData.visuals.message,
                style = Typography.bodyMedium
            )
        }
    }
}