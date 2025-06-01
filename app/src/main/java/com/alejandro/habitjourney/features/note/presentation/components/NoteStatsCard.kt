package com.alejandro.habitjourney.features.note.presentation.components


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.*
import com.alejandro.habitjourney.core.presentation.ui.theme.*

@Composable
fun NoteStatsCard(
    totalNotes: Int,
    totalWords: Int,
    modifier: Modifier = Modifier
) {
    HabitJourneyCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.SpacingMedium),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Total notas
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = totalNotes.toString(),
                    style = Typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AcentoInformativo
                )
                Text(
                    text = stringResource(R.string.total_notes),
                    style = Typography.bodySmall,
                    color = BaseOscura
                )
            }

            // Separador
            VerticalDivider(
                modifier = Modifier.height(48.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            // Total palabras
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = totalWords.toString(),
                    style = Typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AcentoPositivo
                )
                Text(
                    text = stringResource(R.string.total_words),
                    style = Typography.bodySmall,
                    color = BaseOscura
                )
            }
        }
    }
}
