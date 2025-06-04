package com.alejandro.habitjourney.features.settings.presentation.screen


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
                    leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) }, // Solución 1
                    isError = uiState.nameError != null,        // Solución 4a
                    helperText = uiState.nameError,             // Solución 4b
                    enabled = !uiState.isLoading,               // Solución 5 (nombre correcto)
                    modifier = Modifier.fillMaxWidth()
                    // keyboardOptions y visualTransformation usarán sus valores por defecto (Default y None)
                )

// Email field
                HabitJourneyTextField(
                    value = uiState.email,
                    onValueChange = viewModel::updateEmail,
                    label = stringResource(R.string.email_label),
                    leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) }, // Solución 1
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), // Solución 2
                    isError = uiState.emailError != null,        // Solución 4a
                    helperText = uiState.emailError,             // Solución 4b
                    enabled = !uiState.isLoading,               // Solución 5 (nombre correcto)
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
            if (uiState.isLoading) {
                HabitJourneyLoadingOverlay()
            }
        }
    }

    // Handle success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.profile_updated_success),
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