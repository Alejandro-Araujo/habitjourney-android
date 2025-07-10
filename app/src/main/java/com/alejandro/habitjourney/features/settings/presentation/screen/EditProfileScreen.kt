package com.alejandro.habitjourney.features.settings.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.*
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import com.alejandro.habitjourney.features.settings.presentation.viewmodel.EditProfileViewModel
import com.alejandro.habitjourney.features.user.presentation.components.ReauthenticationDialog
import com.alejandro.habitjourney.features.user.presentation.state.ReauthenticationType

/**
 * Pantalla que permite al usuario editar su información de perfil.
 *
 * Muestra un formulario para que el usuario actualice su nombre y dirección de correo electrónico.
 * Gestiona el estado de carga, la reautenticación cuando sea necesaria, y muestra mensajes de éxito
 * o error a través de un Snackbar.
 *
 * @param onNavigateBack Callback para navegar a la pantalla anterior, típicamente tras un cambio exitoso.
 * @param onForceSignOut Callback para navegar al login tras ForceSignOut.
 * @param viewModel El [EditProfileViewModel] que gestiona el estado y la lógica de esta pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    onForceSignOut: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val reauthState by viewModel.reauthState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_edit_profile),
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Dimensions.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
            ) {
                // Instructions
                HabitJourneyCard {
                    Text(
                        text = stringResource(R.string.edit_profile_instructions),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Name field
                HabitJourneyTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    label = stringResource(R.string.full_name_label),
                    leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                    isError = uiState.nameError != null,
                    helperText = uiState.nameError,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                // Email field
                HabitJourneyTextField(
                    value = uiState.email,
                    onValueChange = viewModel::updateEmail,
                    label = stringResource(R.string.email_label),
                    leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = uiState.emailError != null,
                    helperText = uiState.emailError,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                EmailVerificationStatus(
                    isVerified = uiState.isEmailVerified,
                    isLoading = uiState.isLoading,
                    onSendVerification = viewModel::sendVerificationEmail,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.weight(1f))

                // Save button
                HabitJourneyButton(
                    text = stringResource(R.string.save_changes),
                    onClick = { viewModel.saveProfile() },
                    isLoading = uiState.isLoading,
                    enabled = !uiState.isLoading && uiState.hasChanges,
                    type = HabitJourneyButtonType.PRIMARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Loading overlay
            if (uiState.isLoading || reauthState.isLoading) {
                HabitJourneyLoadingOverlay()
            }

            // Reautenticación Dialog
            ReauthenticationDialog(
                state = reauthState,
                onPasswordChange = viewModel::updateReauthPasswordInput,
                onConfirm = {
                    when (reauthState.type) {
                        ReauthenticationType.EMAIL_PASSWORD -> {
                            viewModel.confirmEmailPasswordReauthFromUi()
                        }
                        ReauthenticationType.GOOGLE -> {
                        }
                        null -> {}
                    }
                },
                onDismiss = viewModel::dismissReauthenticationDialog
            )

            // Diálogo de ForceSignOut
            if (uiState.showForceSignOutDialog) {
                EmailVerificationLogoutDialog(
                    onConfirm = {
                        viewModel.executeForceSignOut()
                    },
                    onDismiss = {
                        viewModel.resetForceSignOutState()
                    }
                )
            }
        }
    }

    // Observa cuando el signOut se completó exitosamente
    LaunchedEffect(uiState.logoutCompleted) {
        if (uiState.logoutCompleted) {
            onForceSignOut()
            viewModel.resetLogoutState()
        }
    }

    // Handle success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            val message = if (uiState.emailVerificationSent) {
                context.getString(R.string.profile_updated_success_with_email_verification)
            } else {
                context.getString(R.string.profile_updated_success)
            }

            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )

            viewModel.resetSuccessState()
            onNavigateBack()
        }
    }

    // Handle errors
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    reauthState.errorMessage?.let { reauthError ->
        LaunchedEffect(reauthError) {
            snackbarHostState.showSnackbar(
                message = reauthError,
                duration = SnackbarDuration.Long
            )
            viewModel.dismissReauthenticationDialog()
        }
    }
}

@Composable
fun EmailVerificationLogoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.email_verification_sent_success),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
            ) {
                Text(
                    text = stringResource(R.string.email_verification_dialog_message),
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = stringResource(R.string.email_verification_dialog_instructions),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.go_to_login))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun EmailVerificationStatus(
    isVerified: Boolean,
    isLoading: Boolean,
    onSendVerification: () -> Unit,
    modifier: Modifier = Modifier
) {
    HabitJourneyCard(
        modifier = modifier
    ) {
        if (isVerified) {
            // Estado: Email Verificado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = stringResource(R.string.email_verified_content_description),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimensions.IconSizeSmall)
                )
                Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
                Text(
                    text = stringResource(R.string.email_verified_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            // Estado: Email No Verificado
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.email_not_verified_content_description),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(Dimensions.IconSizeSmall)
                    )
                    Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
                    Text(
                        text = stringResource(R.string.email_not_verified_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Botón de reenvío
                HabitJourneyButton(
                    text = stringResource(R.string.resend_verification_email_button),
                    onClick = onSendVerification,
                    type = HabitJourneyButtonType.SECONDARY,
                    isLoading = isLoading,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
