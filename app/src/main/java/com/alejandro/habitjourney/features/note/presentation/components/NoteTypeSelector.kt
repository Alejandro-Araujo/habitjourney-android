package com.alejandro.habitjourney.features.note.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.NoteType
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoInformativo
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions

@Composable
fun NoteTypeSelector(
    selectedType: NoteType,
    onTypeSelected: (NoteType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.note_type_label),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
        ) {
            NoteType.entries.forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = {
                        onTypeSelected(type)
                    },
                    label = {
                        Text(
                            text = when (type) {
                                NoteType.TEXT -> stringResource(R.string.note_type_text)
                                NoteType.LIST -> stringResource(R.string.note_type_list)
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = when (type) {
                                NoteType.TEXT -> Icons.Default.Description
                                NoteType.LIST -> Icons.Default.Checklist
                            },
                            contentDescription = null,
                            modifier = Modifier.size(Dimensions.IconSizeSmall)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AcentoInformativo,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}
