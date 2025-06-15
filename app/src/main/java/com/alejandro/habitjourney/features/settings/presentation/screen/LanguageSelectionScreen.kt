package com.alejandro.habitjourney.features.settings.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyCard
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import com.alejandro.habitjourney.core.utils.logging.AppLogger
import com.alejandro.habitjourney.features.settings.presentation.state.Language
import com.alejandro.habitjourney.features.settings.presentation.viewmodel.LanguageViewModel

/**
 * Pantalla que permite al usuario seleccionar el idioma de la aplicación.
 *
 * Muestra una lista de los idiomas disponibles y permite al usuario
 * elegir uno, que luego se aplicará en toda la aplicación.
 *
 * @param onNavigateBack Callback para navegar a la pantalla anterior.
 * @param viewModel El [LanguageViewModel] que gestiona el estado y la lógica de esta pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionScreen(
    onNavigateBack: () -> Unit,
    viewModel: LanguageViewModel = hiltViewModel()
) {
    AppLogger.d("LanguageScreen", "ViewModel instanciado: ${viewModel.hashCode()}")

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Lista de idiomas disponibles en la aplicación.
    val languages = Language.allLanguages

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_language),
                        style = Typography.headlineMedium
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = Dimensions.SpacingMedium)
        ) {
            // Tarjeta de instrucciones
            item {
                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
                HabitJourneyCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimensions.SpacingLarge)
                ) {
                    Text(
                        text = stringResource(R.string.language_selection_instructions),
                        style = Typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
            }

            // Lista de idiomas
            items(languages) { language ->
                LanguageItem(
                    language = language,
                    isSelected = language.code == uiState.currentLanguage.code,
                    onClick = {
                        viewModel.updateLanguage(language)
                    }
                )
            }
        }
    }
}

/**
 * Componente que representa un único idioma en la lista de selección.
 *
 * Muestra el nombre del idioma y su nombre nativo, indicando visualmente
 * si es el idioma actualmente seleccionado.
 *
 * @param language El objeto [Language] a mostrar.
 * @param isSelected `true` si este es el idioma actualmente seleccionado.
 * @param onClick Callback que se invoca al pulsar sobre el ítem.
 * @param modifier Modificador para personalizar el layout.
 */
@Composable
private fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.SpacingLarge, vertical = Dimensions.SpacingExtraSmall)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                AcentoInformativo.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(
                width = 2.dp,
                color = AcentoInformativo
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.SpacingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = language.displayName,
                    style = Typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = language.nativeName,
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = AcentoInformativo
                )
            }
        }
    }
}