package com.alejandro.habitjourney.features.note.presentation.components


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoInformativo
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions
import com.alejandro.habitjourney.features.note.presentation.state.NoteFilterType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesFilterDropdown(
    currentFilter: NoteFilterType,
    onFilterSelected: (NoteFilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = !isExpanded }
        ) {
            OutlinedTextField(
                value = getFilterDisplayName(currentFilter),
                onValueChange = { },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                label = { Text(stringResource(R.string.filter_notes_by)) },
                leadingIcon = {
                    Icon(
                        imageVector = getFilterIcon(currentFilter),
                        contentDescription = null,
                        tint = AcentoInformativo
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AcentoInformativo,
                    focusedLabelColor = AcentoInformativo
                )
            )

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                NoteFilterType.entries.forEach { filter ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = getFilterIcon(filter),
                                    contentDescription = null,
                                    modifier = Modifier.size(Dimensions.IconSizeSmall),
                                    tint = if (filter == currentFilter) {
                                        AcentoInformativo
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
                                Text(
                                    text = getFilterDisplayName(filter),
                                    color = if (filter == currentFilter) {
                                        AcentoInformativo
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        },
                        onClick = {
                            onFilterSelected(filter)
                            isExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun getFilterDisplayName(filter: NoteFilterType): String {
    return when (filter) {
        NoteFilterType.ALL -> stringResource(R.string.filter_notes_all)
        NoteFilterType.ACTIVE -> stringResource(R.string.filter_notes_active)
        NoteFilterType.ARCHIVED -> stringResource(R.string.filter_notes_archived)
        NoteFilterType.FAVORITES -> stringResource(R.string.filter_notes_favorites)
    }
}

@Composable
private fun getFilterIcon(filter: NoteFilterType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (filter) {
        NoteFilterType.ALL -> Icons.AutoMirrored.Filled.List
        NoteFilterType.ACTIVE -> Icons.Default.Description
        NoteFilterType.ARCHIVED -> Icons.Default.Archive
        NoteFilterType.FAVORITES -> Icons.Default.Favorite
    }
}