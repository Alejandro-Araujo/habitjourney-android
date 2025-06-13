package com.alejandro.habitjourney.features.note.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import com.alejandro.habitjourney.features.note.domain.model.NoteListItem
import kotlinx.coroutines.delay

@Composable
fun NoteListEditor(
    items: List<NoteListItem>,
    onItemsChanged: (List<NoteListItem>) -> Unit,
    onReorderItems: (Int, Int) -> Unit,
    onToggleItem: (Int) -> Unit,
    onDeleteItem: (Int) -> Unit,
    onIndentChange: (Int, Int) -> Unit,
    onAddItem: () -> Unit,
    modifier: Modifier = Modifier,
    isReadOnly: Boolean = false
) {
    val listState = rememberLazyListState()

    // FocusRequester para el último item
    val lastItemFocusRequester = remember { FocusRequester() }

    // Variable para trackear cuando se añade un item
    var itemCount by remember { mutableIntStateOf(items.size) }

    LaunchedEffect(items.size) {
        if (items.size > itemCount) {
            delay(100)
            listState.animateScrollToItem(items.size - 1)
            delay(50)
            if (items.isNotEmpty()) {
                try {
                    lastItemFocusRequester.requestFocus()
                } catch (_: Exception) {

                }
            }
        }
        itemCount = items.size
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(Dimensions.CornerRadiusSmall),
            border = BorderStroke(
                width = Dimensions.BorderWidth,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaValues.MediumAlpha)
            ),
            color = MaterialTheme.colorScheme.surface
        ) {
            // Lista scrolleable de items
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall),
                contentPadding = PaddingValues(vertical = Dimensions.SpacingSmall)
            ) {
                itemsIndexed(
                    items = items,
                    key = { _, item -> item.id }
                ) { index, item ->
                    NoteListItemEditor(
                        item = item,
                        onItemChanged = { updatedItem ->
                            val newItems = items.toMutableList()
                            newItems[index] = updatedItem
                            onItemsChanged(newItems)
                        },
                        onToggleCompletion = { onToggleItem(index) },
                        onDeleteItem = { onDeleteItem(index) },
                        onIndentChanged = { newIndent ->
                            onIndentChange(index, newIndent)
                        },
                        isReadOnly = isReadOnly,
                        focusRequester = if (index == items.size - 1) lastItemFocusRequester else null,
                        onNext = {
                            // Crear nuevo item al presionar Next en el último
                            if (index == items.size - 1) {
                                onAddItem()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Botón para añadir nuevo item
        if (!isReadOnly) {
            AddNewItemButton(
                onClick = onAddItem,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimensions.SpacingMedium)
            )
        }
    }
}