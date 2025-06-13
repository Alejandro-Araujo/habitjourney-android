package com.alejandro.habitjourney.features.note.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.NoteType
import com.alejandro.habitjourney.core.presentation.ui.theme.*

@Composable
fun NoteTypeSelector(
    selectedType: NoteType,
    onTypeSelected: (NoteType) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth()
    ) {
        NoteType.entries.forEachIndexed { index, type ->
            SegmentedButton(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = NoteType.entries.size
                ),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = AcentoInformativo,
                    activeContentColor = OnPrimary,
                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
                ) {
                    Icon(
                        imageVector = when (type) {
                            NoteType.TEXT -> Icons.Default.Description
                            NoteType.LIST -> Icons.Default.Checklist
                        },
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.IconSizeSmall)
                    )
                    Text(
                        text = when (type) {
                            NoteType.TEXT -> stringResource(R.string.note_type_text)
                            NoteType.LIST -> stringResource(R.string.note_type_list)
                        },
                        style = Typography.bodyMedium
                    )
                }
            }
        }
    }
}