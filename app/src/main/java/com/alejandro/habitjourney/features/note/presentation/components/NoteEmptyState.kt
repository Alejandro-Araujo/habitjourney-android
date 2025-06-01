package com.alejandro.habitjourney.features.note.presentation.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButton
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButtonType
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions
import com.alejandro.habitjourney.features.note.presentation.state.NoteFilterType

@Composable
fun NoteEmptyState(
    currentFilter: NoteFilterType,
    searchQuery: String,
    onCreateNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (title, description) = when {
        searchQuery.isNotBlank() -> {
            stringResource(R.string.empty_search_results) to
                    stringResource(R.string.try_different_search)
        }
        currentFilter == NoteFilterType.ARCHIVED -> {
            stringResource(R.string.empty_archived_notes) to
                    stringResource(R.string.archived_notes_appear_here)
        }
        currentFilter == NoteFilterType.FAVORITES -> {
            stringResource(R.string.empty_favorite_notes) to
                    stringResource(R.string.favorite_notes_appear_here)
        }
        else -> {
            stringResource(R.string.empty_notes_title) to
                    stringResource(R.string.empty_notes_description)
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.StickyNote2,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (searchQuery.isBlank() && currentFilter != NoteFilterType.ARCHIVED) {
            Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

            HabitJourneyButton(
                text = stringResource(R.string.create_first_note),
                onClick = onCreateNote,
                type = HabitJourneyButtonType.PRIMARY,
                leadingIcon = Icons.Default.Add
            )
        }
    }
}
