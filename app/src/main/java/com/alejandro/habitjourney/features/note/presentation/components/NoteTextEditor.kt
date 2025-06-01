package com.alejandro.habitjourney.features.note.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoInformativo
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions


@Composable
fun NoteTextEditor(
    content: String,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isReadOnly: Boolean = false
) {
    OutlinedTextField(
        value = content,
        onValueChange = onContentChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimensions.NoteEditorMinHeight),
        placeholder = {
            Text(stringResource(R.string.note_content_hint))
        },
        readOnly = isReadOnly,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AcentoInformativo,
            focusedLabelColor = AcentoInformativo
        )
    )
}