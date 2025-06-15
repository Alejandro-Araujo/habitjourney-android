package com.alejandro.habitjourney.features.settings.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.*
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import com.alejandro.habitjourney.features.settings.presentation.state.ThemeMode
import com.alejandro.habitjourney.features.settings.presentation.viewmodel.SettingsViewModel
import com.alejandro.habitjourney.features.user.domain.model.User

/**
 * Pantalla principal de Configuración de la aplicación.
 *
 * Muestra las opciones de perfil, cuenta y apariencia. Permite al usuario
 * navegar a otras pantallas de configuración, cambiar el tema, cerrar sesión
 * y eliminar su cuenta.
 *
 * @param onNavigateBack Callback para navegar a la pantalla anterior.
 * @param onNavigateToEditProfile Callback para navegar a la pantalla de edición de perfil.
 * @param onNavigateToChangePassword Callback para navegar a la pantalla de cambio de contraseña.
 * @param onNavigateToLanguage Callback para navegar a la pantalla de selección de idioma.
 * @param onNavigateToAuth Callback para navegar a la pantalla de autenticación, típicamente después de cerrar sesión.
 * @param viewModel El [SettingsViewModel] que gestiona el estado y la lógica de esta pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToAuth: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estados para los diálogos
    var showThemeDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
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
            contentPadding = PaddingValues(vertical = Dimensions.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
        ) {
            // Profile Section
            item {
                ProfileSection(
                    user = uiState.user,
                    onEditProfile = onNavigateToEditProfile
                )
            }

            // Account Settings
            item {
                SettingsSection(title = stringResource(R.string.settings_section_account)) {
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = stringResource(R.string.settings_change_password),
                        subtitle = stringResource(R.string.settings_change_password_subtitle),
                        onClick = onNavigateToChangePassword
                    )
                }
            }

            // App Settings
            item {
                SettingsSection(title = stringResource(R.string.settings_section_app)) {
                    // Theme
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = stringResource(R.string.settings_theme),
                        subtitle = when (uiState.currentTheme) {
                            ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                            ThemeMode.DARK -> stringResource(R.string.theme_dark)
                            ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                        },
                        onClick = { showThemeDialog = true }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = Dimensions.SpacingMedium),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Language
                    SettingsItem(
                        icon = Icons.Default.Language,
                        title = stringResource(R.string.settings_language),
                        subtitle = uiState.currentLanguage.displayName,
                        onClick = onNavigateToLanguage
                    )
                }
            }

            // Danger Zone
            item {
                SettingsSection(
                    title = stringResource(R.string.settings_section_danger),
                    titleColor = MaterialTheme.colorScheme.error
                ) {
                    SettingsItem(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        title = stringResource(R.string.settings_logout),
                        subtitle = stringResource(R.string.settings_logout_subtitle),
                        titleColor = MaterialTheme.colorScheme.error,
                        onClick = { showLogoutDialog = true }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = Dimensions.SpacingMedium),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    SettingsItem(
                        icon = Icons.Default.DeleteForever,
                        title = stringResource(R.string.settings_delete_account),
                        subtitle = stringResource(R.string.settings_delete_account_subtitle),
                        titleColor = MaterialTheme.colorScheme.error,
                        onClick = { showDeleteAccountDialog = true }
                    )
                }
            }

            // App Info
            item {
                Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
                AppInfoSection()
            }
        }
    }

    // Theme Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = uiState.currentTheme,
            onThemeSelected = { theme ->
                viewModel.updateTheme(theme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    // Delete Account Dialog
    if (showDeleteAccountDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.dialog_delete_account_title),
            message = stringResource(R.string.dialog_delete_account_message),
            confirmText = stringResource(R.string.dialog_delete_account_confirm),
            dismissText = stringResource(R.string.cancel),
            onConfirm = {
                showDeleteAccountDialog = false
                viewModel.deleteAccount()
            },
            onDismiss = { showDeleteAccountDialog = false },
            isDangerous = true
        )
    }

    // Logout Dialog
    if (showLogoutDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.dialog_logout_title),
            message = stringResource(R.string.dialog_logout_message),
            confirmText = stringResource(R.string.dialog_logout_confirm),
            dismissText = stringResource(R.string.cancel),
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    // Handle navigation events
    LaunchedEffect(uiState.navigateToAuth) {
        if (uiState.navigateToAuth) {
            onNavigateToAuth()
            viewModel.onNavigationHandled()
        }
    }

    // Handle messages
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessage()
        }
    }
}

/**
 * Muestra una sección con la información del perfil del usuario y un botón para editarlo.
 * @param user El objeto [User] a mostrar, o null.
 * @param onEditProfile Callback para navegar a la pantalla de edición de perfil.
 * @param modifier Modificador para el layout.
 */
@Composable
private fun ProfileSection(
    user: User?,
    onEditProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    HabitJourneyCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Surface(
                modifier = Modifier
                    .size(Dimensions.IconSizeExtraLarge)
                    .clip(CircleShape),
                color = AcentoInformativo
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user?.name?.firstOrNull()?.uppercase() ?: "U",
                        style = Typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

            // Name
            Text(
                text = user?.name ?: "Usuario",
                style = Typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Email
            Text(
                text = user?.email ?: "",
                style = Typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

            // Edit button
            HabitJourneyButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.settings_edit_profile),
                onClick = onEditProfile,
                type = HabitJourneyButtonType.SECONDARY,
                leadingIcon = Icons.Default.Edit,
            )
        }
    }
}

/**
 * Componente genérico para crear una sección de configuración con un título.
 * @param title El título de la sección.
 * @param titleColor El color del título.
 * @param content El contenido Composable de la sección.
 * @param modifier Modificador para el layout.
 */
@Composable
private fun SettingsSection(
    modifier: Modifier = Modifier,
    title: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = Typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = titleColor,
            modifier = Modifier.padding(
                horizontal = Dimensions.SpacingLarge,
                vertical = Dimensions.SpacingSmall
            )
        )

        HabitJourneyCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                content()
            }
        }
    }
}

/**
 * Un ítem de menú individual para una pantalla de configuración.
 * @param icon El icono a mostrar a la izquierda.
 * @param title El título principal del ítem.
 * @param subtitle Un subtítulo opcional para dar más contexto.
 * @param titleColor El color a aplicar al título.
 * @param onClick Callback que se invoca al pulsar el ítem.
 * @param modifier Modificador para el layout.
 */
@Composable
private fun SettingsItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(Dimensions.SpacingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (titleColor == MaterialTheme.colorScheme.error) {
                titleColor
            } else {
                AcentoInformativo
            },
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(Dimensions.SpacingMedium))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = Typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Dimensions.IconSizeButton)
        )
    }
}

/**
 * Diálogo para la selección de tema (Claro, Oscuro, Sistema).
 * @param currentTheme El tema actualmente seleccionado.
 * @param onThemeSelected Callback invocado cuando se selecciona un nuevo tema.
 * @param onDismiss Callback invocado para cerrar el diálogo.
 */
@Composable
private fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.settings_theme),
                style = Typography.headlineSmall
            )
        },
        text = {
            Column {
                ThemeMode.entries.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = Dimensions.SpacingSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = theme == currentTheme,
                            onClick = { onThemeSelected(theme) }
                        )
                        Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
                        Text(
                            text = when (theme) {
                                ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                                ThemeMode.DARK -> stringResource(R.string.theme_dark)
                                ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

/**
 * Muestra información de la aplicación, como el nombre y la versión.
 * @param modifier Modificador para el layout.
 */
@Composable
private fun AppInfoSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = Typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.app_version),
            style = Typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
        Text(
            text = stringResource(R.string.app_author),
            style = Typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Diálogo de confirmación genérico para acciones peligrosas o importantes.
 * @param title Título del diálogo.
 * @param message Mensaje del cuerpo del diálogo.
 * @param confirmText Texto del botón de confirmación.
 * @param dismissText Texto del botón de cancelación.
 * @param onConfirm Callback para la acción de confirmación.
 * @param onDismiss Callback para la acción de cancelación o para cerrar el diálogo.
 * @param isDangerous `true` si la acción es destructiva, para colorear el botón de confirmación en rojo.
 */
@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDangerous: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = Typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                style = Typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = if (isDangerous) {
                    ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    ButtonDefaults.textButtonColors()
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}
