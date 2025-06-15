package com.alejandro.habitjourney.core.presentation.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.theme.*

data class FilterOption<T>(
    val value: T,
    val label: String,
    val icon: ImageVector? = null
)

/**
 * TopAppBar con búsqueda integrada y filtros dropdown.
 * Transiciones animadas entre modo normal y búsqueda.
 *
 * @param T Tipo de datos para las opciones de filtro
 * @param title Título cuando no está en modo búsqueda
 * @param isSearchActive Si está en modo búsqueda
 * @param searchQuery Query actual de búsqueda
 * @param currentFilter Filtro actualmente seleccionado
 * @param filterOptions Lista de opciones disponibles para filtrar
 * @param additionalActions Acciones adicionales en el toolbar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> HabitJourneySearchableTopBar(
    title: String,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchToggle: () -> Unit,
    currentFilter: T,
    filterOptions: List<FilterOption<T>>,
    onFilterSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    additionalActions: @Composable () -> Unit = {}
) {
    var showFilterMenu by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    TopAppBar(
        title = {
            AnimatedContent(
                targetState = isSearchActive,
                transitionSpec = {
                    (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                            (slideOutHorizontally { width -> -width } + fadeOut())
                },
                label = "SearchAnimation"
            ) { isSearching ->
                if (isSearching) {
                    // Modo búsqueda
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.search_placeholder),
                                style = Typography.bodyLarge
                            )
                        },
                        leadingIcon = {
                            IconButton(onClick = {
                                onSearchToggle()
                                keyboardController?.hide()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.close_search),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.clear_search),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { keyboardController?.hide() }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = AcentoInformativo
                        ),
                        textStyle = Typography.bodyLarge
                    )

                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                } else {
                    // Modo normal
                    Text(
                        text = title,
                        style = Typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        actions = {
            if (!isSearchActive) {
                // Acciones adicionales (si las hay)
                additionalActions()

                // Botón de filtro
                Box {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = stringResource(R.string.filter),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        filterOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        option.icon?.let { icon ->
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                modifier = Modifier.size(Dimensions.IconSizeSmall),
                                                tint = if (option.value == currentFilter) {
                                                    AcentoInformativo
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
                                        }
                                        Text(
                                            text = option.label,
                                            style = Typography.bodyLarge,
                                            color = if (option.value == currentFilter) {
                                                AcentoInformativo
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                },
                                onClick = {
                                    onFilterSelected(option.value)
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }

                // Botón de búsqueda
                IconButton(onClick = onSearchToggle) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search_habits),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}