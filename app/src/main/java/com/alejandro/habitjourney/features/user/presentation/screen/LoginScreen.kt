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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.alejandro.habitjourney.features.user.presentation.state.LoginState
import com.alejandro.habitjourney.features.user.presentation.viewmodel.LoginViewModel
import kotlinx.coroutines.launch
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButton
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButtonType
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyTextField
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneySnackbarHost
import com.alejandro.habitjourney.core.presentation.ui.theme.*
import androidx.compose.material3.SnackbarHostState

/**
 * Pantalla de inicio de sesión.
 *
 * Permite al usuario introducir sus credenciales (correo electrónico y contraseña)
 * para iniciar sesión en la aplicación. Muestra el estado del proceso (carga, éxito, error)
 * y ofrece navegación a la pantalla de registro.
 *
 * @param onNavigateToRegister Lambda que se invoca para navegar a la pantalla de registro.
 * @param onLoginSuccess Lambda que se invoca tras un inicio de sesión exitoso, típicamente para navegar a la pantalla principal.
 * @param viewModel La instancia de [LoginViewModel] inyectada por Hilt.
 */
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val loginState by viewModel.loginState.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val isPasswordVisible by viewModel.isPasswordVisible.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val isGoogleSignInLoading by viewModel.isGoogleSignInLoading.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.triggerOneTapSignIn()
    }

    // Manejo del estado
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> {
                onLoginSuccess()
                viewModel.resetState()
            }
            is LoginState.Error -> {
                val errorMessage = (loginState as LoginState.Error).message
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = errorMessage,
                        actionLabel = context.getString(R.string.error_action_label),
                        duration = SnackbarDuration.Long
                    )
                }
                viewModel.resetState()
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

            // Logo de la app
            Image(
                painter = painterResource(id = R.drawable.logo_habitjourney),
                contentDescription = stringResource(R.string.app_logo_content_description),
                modifier = Modifier
                    .size(280.dp)
                    .padding(bottom = Dimensions.SpacingMedium),
                contentScale = ContentScale.Fit
            )

            // Header
            Text(
                text = stringResource(R.string.welcome_back_title),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaValues.MediumAlpha),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

            // Formulario
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
                    // Campo Email
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

                    // Campo Contraseña
                    HabitJourneyTextField(
                        value = password,
                        onValueChange = viewModel::onPasswordChanged,
                        label = stringResource(R.string.password_label),
                        modifier = Modifier.fillMaxWidth(),
                        isError = passwordError != null,
                        helperText = passwordError,
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.login()
                            }
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

                    Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

                    // Botón de Login
                    HabitJourneyButton(
                        text = stringResource(R.string.login_button_text),
                        onClick = viewModel::login,
                        type = HabitJourneyButtonType.PRIMARY,
                        enabled = loginState !is LoginState.Loading,
                        isLoading = loginState is LoginState.Loading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

                    // Botón de Google Sign-In
                    HabitJourneyButton(
                        text = stringResource(R.string.sign_in_with_google_text),
                        onClick = {
                            viewModel.signInWithGoogleButton()
                        },
                        type = HabitJourneyButtonType.SECONDARY,
                        enabled = loginState !is LoginState.Loading && !isGoogleSignInLoading,
                        isLoading = isGoogleSignInLoading,
                        leadingIconPainter = painterResource(id = R.drawable.ic_google),
                        iconContentDescription = stringResource(R.string.content_description_google_logo),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

            // Enlace a registro
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.no_account_question),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaValues.MediumAlpha)
                )
                HabitJourneyButton(
                    text = stringResource(R.string.register_link_text),
                    onClick = onNavigateToRegister,
                    type = HabitJourneyButtonType.TERTIARY
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
        }
    }
}

