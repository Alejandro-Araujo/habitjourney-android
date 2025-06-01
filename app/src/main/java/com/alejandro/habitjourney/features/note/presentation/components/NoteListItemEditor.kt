package com.alejandro.habitjourney.features.note.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoPositivo
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions
import com.alejandro.habitjourney.core.presentation.ui.theme.ErrorColor
import com.alejandro.habitjourney.features.note.domain.model.NoteListItem

@Composable
fun NoteListItemEditor(
    item: NoteListItem,
    onItemChanged: (NoteListItem) -> Unit,
    onToggleCompletion: () -> Unit,
    onDeleteItem: () -> Unit,
    onIndentChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isReadOnly: Boolean = false,
    focusRequester: FocusRequester? = null,
    onNext: (() -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.SpacingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Checkbox(
            checked = item.isCompleted,
            onCheckedChange = { onToggleCompletion() },
            enabled = !isReadOnly,
            colors = CheckboxDefaults.colors(
                checkedColor = AcentoPositivo,
                uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        )

        Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))

        // Campo de texto
        BasicTextField(
            value = item.text,
            onValueChange = { newText ->
                onItemChanged(item.copy(text = newText))
            },
            modifier = Modifier
                .weight(1f)
                .let { modifier ->
                    if (focusRequester != null) {
                        modifier.focusRequester(focusRequester)
                    } else modifier
                }
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                },
            readOnly = isReadOnly,
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                textDecoration = if (item.isCompleted) {
                    TextDecoration.LineThrough
                } else {
                    null
                },
                color = if (item.isCompleted) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontSize = MaterialTheme.typography.bodyLarge.fontSize
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    onNext?.invoke()
                }
            ),
            decorationBox = { innerTextField ->
                if (item.text.isEmpty() && !isFocused) {

                }
                innerTextField()
            }
        )

        // Botón eliminar
        if (!isReadOnly && isFocused) {
            Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))

            IconButton(
                onClick = onDeleteItem,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_item),
                    tint = ErrorColor,
                    modifier = Modifier.size(Dimensions.IconSizeSmall)
                )
            }
        }
    }
}