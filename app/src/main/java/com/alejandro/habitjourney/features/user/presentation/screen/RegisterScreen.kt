package com.alejandro.habitjourney.features.user.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.features.user.presentation.state.RegisterState
import com.alejandro.habitjourney.features.user.presentation.viewmodel.RegisterViewModel
import kotlinx.coroutines.launch
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButton
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButtonType
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyTextField
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneySnackbarHost
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Pantalla de registro de usuario.
 *
 * Permite al usuario introducir sus datos (nombre, correo electrónico, contraseña y confirmación de contraseña)
 * para crear una nueva cuenta en la aplicación. Muestra el estado del proceso (carga, éxito, error)
 * y ofrece navegación a la pantalla de inicio de sesión.
 *
 * @param onNavigateToLogin Lambda que se invoca para navegar a la pantalla de inicio de sesión.
 * @param onRegisterSuccess Lambda que se invoca tras un registro exitoso, típicamente para navegar a la pantalla principal o de inicio de sesión.
 * @param viewModel La instancia de [RegisterViewModel] inyectada por Hilt.
 */
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val registerState by viewModel.registerState.collectAsState()
    val name by viewModel.name.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val isPasswordVisible by viewModel.isPasswordVisible.collectAsState()
    val isConfirmPasswordVisible by viewModel.isConfirmPasswordVisible.collectAsState()
    val nameError by viewModel.nameError.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val confirmPasswordError by viewModel.confirmPasswordError.collectAsState()
    val isGoogleSignInLoading by viewModel.isGoogleSignInLoading.collectAsState()
    val focusManager = LocalFocusManager.current

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val successfulAccountCreationMessage = stringResource(R.string.registration_success_check_email)
    val dialogSuccessTitle = stringResource(R.string.dialog_success_title)
    val dialogErrorTitle = stringResource(R.string.dialog_error_title)

    LaunchedEffect(registerState) {
        when (registerState) {
            is RegisterState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = successfulAccountCreationMessage,
                        actionLabel = dialogSuccessTitle,
                        duration = SnackbarDuration.Short
                    )
                }
                delay(2000)
                viewModel.resetState()
                onRegisterSuccess()
            }
            is RegisterState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = (registerState as RegisterState.Error).message,
                        actionLabel = dialogErrorTitle,
                        duration = SnackbarDuration.Long
                    )
                }
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = {
            HabitJourneySnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = Dimensions.SpacingMedium)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = Dimensions.SpacingSmall),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

            Image(
                painter = painterResource(id = R.drawable.logo_habitjourney),
                contentDescription = stringResource(R.string.app_logo_content_description),
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = Dimensions.SpacingMedium),
                contentScale = ContentScale.Fit
            )

            Text(
                text = stringResource(R.string.create_account_title),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

            Text(
                text = stringResource(R.string.register_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaValues.MediumAlpha),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.ElevationLevel1),
                shape = RoundedCornerShape(Dimensions.CornerRadius)
            ) {
                Column(
                    modifier = Modifier.padding(Dimensions.SpacingLarge),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
                ) {
                    HabitJourneyTextField(
                        value = name,
                        onValueChange = viewModel::onNameChanged,
                        label = stringResource(R.string.full_name_label),
                        modifier = Modifier.fillMaxWidth(),
                        isError = nameError != null,
                        helperText = nameError,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = stringResource(R.string.content_description_person_icon),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    HabitJourneyTextField(
                        value = email,
                        onValueChange = viewModel::onEmailChanged,
                        label = stringResource(R.string.email_label),
                        modifier = Modifier.fillMaxWidth(),
                        isError = emailError != null,
                        helperText = emailError,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = stringResource(R.string.content_description_email_icon),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    HabitJourneyTextField(
                        value = password,
                        onValueChange = viewModel::onPasswordChanged,
                        label = stringResource(R.string.password_label),
                        modifier = Modifier.fillMaxWidth(),
                        isError = passwordError != null,
                        helperText = passwordError ?: stringResource(R.string.password_hint_text),
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = stringResource(R.string.content_description_lock_icon),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = viewModel::togglePasswordVisibility) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = if (isPasswordVisible)
                                        stringResource(R.string.hide_password_content_description)
                                    else stringResource(R.string.show_password_content_description),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaValues.MediumAlpha)
                                )
                            }
                        }
                    )

                    HabitJourneyTextField(
                        value = confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChanged,
                        label = stringResource(R.string.confirm_password_label),
                        modifier = Modifier.fillMaxWidth(),
                        isError = confirmPasswordError != null,
                        helperText = confirmPasswordError,
                        singleLine = true,
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.register()
                            }
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = stringResource(R.string.content_description_confirm_password_icon),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = viewModel::toggleConfirmPasswordVisibility) {
                                Icon(
                                    imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = if (isConfirmPasswordVisible)
                                        stringResource(R.string.hide_password_content_description)
                                    else stringResource(R.string.show_password_content_description),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaValues.MediumAlpha)
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

                    HabitJourneyButton(
                        text = stringResource(R.string.register_button_text),
                        onClick = viewModel::register,
                        type = HabitJourneyButtonType.PRIMARY,
                        enabled = registerState !is RegisterState.Loading,
                        isLoading = registerState is RegisterState.Loading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

                    HabitJourneyButton(
                        text = stringResource(R.string.sign_up_with_google),
                        onClick = viewModel::registerWithGoogle,
                        type = HabitJourneyButtonType.SECONDARY,
                        enabled = registerState !is RegisterState.Loading && !isGoogleSignInLoading,
                        isLoading = isGoogleSignInLoading,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIconPainter = painterResource(id = R.drawable.ic_google),
                        iconContentDescription = stringResource(R.string.content_description_google_logo),
                    )

                    Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

            Text(
                text = stringResource(R.string.terms_and_privacy_text),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaValues.MediumAlpha),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Dimensions.SpacingMedium)
            )

            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.already_have_account_question),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaValues.MediumAlpha)
                )
                HabitJourneyButton(
                    text = stringResource(R.string.login_here_link_text),
                    onClick = onNavigateToLogin,
                    type = HabitJourneyButtonType.TERTIARY
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
        }
    }
}