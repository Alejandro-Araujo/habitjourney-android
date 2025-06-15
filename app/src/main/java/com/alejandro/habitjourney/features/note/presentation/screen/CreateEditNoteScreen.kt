package com.alejandro.habitjourney.features.note.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.NoteType
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButton
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButtonType
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyLoadingOverlay
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoInformativo
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoPositivo
import com.alejandro.habitjourney.core.presentation.ui.theme.AlphaValues
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions
import com.alejandro.habitjourney.core.presentation.ui.theme.ErrorColor
import com.alejandro.habitjourney.core.presentation.ui.theme.OnPrimary
import com.alejandro.habitjourney.core.presentation.ui.theme.Typography
import com.alejandro.habitjourney.features.note.domain.model.NoteListItem
import com.alejandro.habitjourney.features.note.presentation.viewmodel.CreateEditNoteViewModel
import kotlinx.coroutines.delay


/**
 * Pantalla principal para crear o editar una nota.
 *
 * Esta pantalla se adapta para funcionar en tres modos: creación, edición y solo lectura.
 * Proporciona un formulario para el título y el contenido (texto o lista de tareas),
 * y gestiona las interacciones del usuario para guardar, actualizar y navegar.
 *
 * @param noteId El ID de la nota a editar. Si es `null`, la pantalla entra en modo de creación.
 * @param isReadOnly `true` si la pantalla debe mostrarse en modo de solo lectura, deshabilitando la edición.
 * @param onNavigateBack Callback para manejar la acción de volver a la pantalla anterior.
 * @param viewModel El [CreateEditNoteViewModel] que gestiona el estado y la lógica de esta pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditNoteScreen(
    noteId: Long?,
    isReadOnly: Boolean = false,
    onNavigateBack: () -> Unit,
    viewModel: CreateEditNoteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Inicializar nota al entrar en la pantalla
    LaunchedEffect(noteId, isReadOnly) {
        viewModel.initializeNote(noteId, isReadOnly)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (noteId == null) {
                            stringResource(R.string.create_note)
                        } else {
                            stringResource(R.string.edit_note)
                        },
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                },
                actions = {
                    if (!uiState.isReadOnly) {
                        IconButton(
                            onClick = { viewModel.toggleFavorite() }
                        ) {
                            Icon(
                                imageVector = if (uiState.isFavorite) {
                                    Icons.Default.Favorite
                                } else {
                                    Icons.Default.FavoriteBorder
                                },
                                contentDescription = if (uiState.isFavorite) {
                                    stringResource(R.string.unfavorite_note)
                                } else {
                                    stringResource(R.string.favorite_note)
                                },
                                tint = if (uiState.isFavorite) {
                                    AcentoPositivo
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(Dimensions.SpacingMedium),
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
            ) {
                // Selector de tipo de nota
                if (!uiState.isReadOnly) {
                    NoteTypeSelector(
                        selectedType = uiState.noteType,
                        onTypeSelected = viewModel::updateNoteType
                    )
                }

                // Campo título
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = viewModel::updateTitle,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.note_title_hint)) },
                    readOnly = uiState.isReadOnly,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AcentoInformativo,
                        focusedLabelColor = AcentoInformativo
                    )
                )

                // Editor de contenido
                when (uiState.noteType) {
                    NoteType.TEXT -> {
                        NoteTextEditor(
                            content = uiState.content,
                            onContentChange = viewModel::updateContent,
                            isReadOnly = uiState.isReadOnly,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    NoteType.LIST -> {
                        NoteListEditor(
                            items = uiState.listItems,
                            onItemsChanged = viewModel::updateListItems,
                            onToggleItem = viewModel::toggleItemCompletion,
                            onDeleteItem = viewModel::deleteListItem,
                            onAddItem = viewModel::addListItem,
                            modifier = Modifier.weight(1f),
                            isReadOnly = uiState.isReadOnly
                        )
                    }
                }

                // BOTÓN DE GUARDAR
                if (!uiState.isReadOnly) {
                    HabitJourneyButton(
                        text = if (noteId == null) {
                            stringResource(R.string.create_note)
                        } else {
                            stringResource(R.string.save_note)
                        },
                        onClick = {
                            viewModel.saveNote {
                                keyboardController?.hide()
                                onNavigateBack()
                            }
                        },
                        type = HabitJourneyButtonType.PRIMARY,
                        enabled = uiState.canSave && !uiState.isSaving,
                        isLoading = uiState.isSaving,
                        leadingIcon = Icons.Default.Save,
                        iconContentDescription = stringResource(R.string.save_note),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Espaciado extra al final
                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
            }

            // Overlay de carga
            if (uiState.isLoading) {
                HabitJourneyLoadingOverlay()
            }
        }
    }

    // Mostrar errores
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }
}

/**
 * Componente de editor para notas de tipo texto.
 * @param content El contenido de texto actual.
 * @param onContentChange Callback para notificar cambios en el contenido.
 * @param modifier Modificador para personalizar el layout.
 * @param isReadOnly `true` para deshabilitar la edición.
 */
@Composable
private fun NoteTextEditor(
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

/**
 * Componente de editor para notas de tipo lista (checklist).
 *
 * @param items La lista de [NoteListItem] a mostrar.
 * @param onItemsChanged Callback para notificar cambios en toda la lista.
 * @param onToggleItem Callback para cambiar el estado de completado de un ítem.
 * @param onDeleteItem Callback para eliminar un ítem.
 * @param onAddItem Callback para añadir un nuevo ítem a la lista.
 * @param modifier Modificador para personalizar el layout.
 * @param isReadOnly `true` para deshabilitar la edición.
 */
@Composable
private fun NoteListEditor(
    items: List<NoteListItem>,
    onItemsChanged: (List<NoteListItem>) -> Unit,
    onToggleItem: (Int) -> Unit,
    onDeleteItem: (Int) -> Unit,
    onAddItem: () -> Unit,
    modifier: Modifier = Modifier,
    isReadOnly: Boolean = false
) {
    val listState = rememberLazyListState()

    val lastItemFocusRequester = remember { FocusRequester() }

    var itemCount by remember { mutableIntStateOf(items.size) }

    // Efecto para hacer scroll y solicitar foco al añadir un nuevo ítem.
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
                        modifier = Modifier.fillMaxWidth(),
                        isReadOnly = isReadOnly,
                        focusRequester = if (index == items.size - 1) lastItemFocusRequester else null,
                        onNext = {
                            if (index == items.size - 1) {
                                onAddItem()
                            }
                        }
                    )
                }
            }
        }

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


/**
 * Componente de editor para un único ítem de una lista de tareas.
 *
 * @param item El [NoteListItem] a mostrar y editar.
 * @param onItemChanged Callback para notificar cambios en el texto del ítem.
 * @param onToggleCompletion Callback para cambiar el estado de completado.
 * @param onDeleteItem Callback para eliminar el ítem.
 * @param modifier Modificador para personalizar el layout.
 * @param isReadOnly `true` para deshabilitar la edición.
 * @param focusRequester Requester para solicitar el foco programáticamente.
 * @param onNext Callback invocado cuando el usuario presiona la acción "Next" del teclado.
 */
@Composable
private fun NoteListItemEditor(
    item: NoteListItem,
    onItemChanged: (NoteListItem) -> Unit,
    onToggleCompletion: () -> Unit,
    onDeleteItem: () -> Unit,
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
        BasicTextField(
            value = item.text,
            onValueChange = { newText ->
                onItemChanged(item.copy(text = newText))
            },
            modifier = Modifier
                .weight(1f)
                .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
                .onFocusChanged { focusState -> isFocused = focusState.isFocused },
            readOnly = isReadOnly,
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null,
                color = if (item.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.onSurface,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { onNext?.invoke() }),
        )
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


/**
 * Componente que muestra una fila de botones segmentados para seleccionar el tipo de nota.
 * @param selectedType El [NoteType] actualmente seleccionado.
 * @param onTypeSelected Callback para notificar la selección de un nuevo tipo.
 * @param modifier Modificador para personalizar el layout.
 */
@Composable
private fun NoteTypeSelector(
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
                shape = SegmentedButtonDefaults.itemShape(index = index, count = NoteType.entries.size),
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

/**
 * Botón estilizado para añadir un nuevo ítem a la lista.
 * @param onClick Callback que se invoca al pulsar el botón.
 * @param modifier Modificador para personalizar el layout.
 */
@Composable
private fun AddNewItemButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HabitJourneyButton(
        text = stringResource(R.string.add_list_item),
        onClick = onClick,
        type = HabitJourneyButtonType.TERTIARY,
        leadingIcon = Icons.Default.Add,
        modifier = modifier
    )
}

