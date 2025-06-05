package com.alejandro.habitjourney.features.settings.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.*
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import com.alejandro.habitjourney.features.settings.presentation.viewmodel.ChangePasswordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_change_password),
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
                        text = stringResource(R.string.change_password_instructions),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Current password
                var currentPasswordVisible by remember { mutableStateOf(false) } // Para alternar visibilidad
                HabitJourneyTextField(
                    value = uiState.currentPassword,
                    onValueChange = viewModel::updateCurrentPassword,
                    label = stringResource(R.string.label_current_password),
                    leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) }, // Solución 1
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), // Solución 2
                    visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(), // Solución 3
                    trailingIcon = { // Opcional: para alternar visibilidad
                        val image = if (currentPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                        IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = if (currentPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña")
                        }
                    },
                    isError = uiState.currentPasswordError != null, // Solución 4a
                    helperText = uiState.currentPasswordError,      // Solución 4b
                    enabled = !uiState.isLoading,                   // Solución 5 (nombre correcto)
                    modifier = Modifier.fillMaxWidth()
                )

// New password (aplicar cambios similares)
                var newPasswordVisible by remember { mutableStateOf(false) }
                HabitJourneyTextField(
                    value = uiState.newPassword,
                    onValueChange = viewModel::updateNewPassword,
                    label = stringResource(R.string.label_new_password),
                    leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (newPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = if (newPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña")
                        }
                    },
                    isError = uiState.newPasswordError != null,
                    helperText = uiState.newPasswordError,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

// Confirm new password (aplicar cambios similares)
                var confirmPasswordVisible by remember { mutableStateOf(false) }
                HabitJourneyTextField(
                    value = uiState.confirmPassword,
                    onValueChange = viewModel::updateConfirmPassword,
                    label = stringResource(R.string.label_confirm_password),
                    leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = if (confirmPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña")
                        }
                    },
                    isError = uiState.confirmPasswordError != null,
                    helperText = uiState.confirmPasswordError,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                // Password requirements
                PasswordRequirements()

                Spacer(modifier = Modifier.weight(1f))

                // Change password button
                HabitJourneyButton(
                    text = stringResource(R.string.change_password),
                    onClick = { viewModel.changePassword() },
                    isLoading = uiState.isLoading,
                    enabled = !uiState.isLoading && uiState.isValid,
                    type = HabitJourneyButtonType.PRIMARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Loading overlay
            if (uiState.isLoading) {
                HabitJourneyLoadingOverlay()
            }
        }
    }

    // Handle success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.password_changed_success),
                duration = SnackbarDuration.Short
            )
            onNavigateBack()
        }
    }

    // Handle errors
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
}

@Composable
private fun PasswordRequirements(
    modifier: Modifier = Modifier
) {
    HabitJourneyCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingExtraSmall)
        ) {
            Text(
                text = stringResource(R.string.password_requirements_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "• ${stringResource(R.string.password_requirement_length)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "• ${stringResource(R.string.password_requirement_letter)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}