package com.alejandro.habitjourney.features.user.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.* // Mantenemos Material3 para Card, Icon, IconButton, etc.
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.features.user.presentation.state.LoginState
import com.alejandro.habitjourney.features.user.presentation.viewmodel.LoginViewModel

// Importa tus componentes personalizados
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButton
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButtonType
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyTextField

// Importa tus dimensiones y colores personalizados
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions
import com.alejandro.habitjourney.core.presentation.ui.theme.BaseClara
import com.alejandro.habitjourney.core.presentation.ui.theme.BaseOscura
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoInformativo
import com.alejandro.habitjourney.core.presentation.ui.theme.InactivoDeshabilitado
import com.alejandro.habitjourney.core.presentation.ui.theme.Error
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoUrgente // Para el mensaje de error general

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
    val focusManager = LocalFocusManager.current

    // Manejo del estado de éxito
    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BaseClara) // Usa tu color BaseClara para el fondo
            .verticalScroll(rememberScrollState())
            .padding(Dimensions.SpacingLarge), // Usa Dimensions.SpacingLarge para el padding de la pantalla
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(Dimensions.ButtonHeight)) // Usa una dimensión adecuada aquí

        // Header
        Text(
            text = stringResource(R.string.welcome_back_title),
            style = MaterialTheme.typography.displayLarge, // H1: 24sp
            color = BaseOscura, // Tu BaseOscura para el texto principal
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall)) // SpacingSmall = 8dp

        Text(
            text = stringResource(R.string.login_subtitle),
            style = MaterialTheme.typography.bodyLarge, // Cuerpo: 16sp
            color = InactivoDeshabilitado, // Tu A0AEC0 para textos secundarios/deshabilitados
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimensions.ButtonHeight - Dimensions.SpacingSmall))

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
                    )
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
                    // Trailing icon para mostrar/ocultar contraseña
                    trailingIcon = {
                        IconButton(onClick = viewModel::togglePasswordVisibility) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = if (isPasswordVisible) stringResource(R.string.hide_password_content_description)
                                else stringResource(R.string.show_password_content_description),
                                tint = InactivoDeshabilitado
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
                    isLoading = loginState is LoginState.Loading
                )

                // Mensaje de error general
                AnimatedVisibility(
                    visible = loginState is LoginState.Error,
                    enter = slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300)),
                    exit = slideOutVertically(
                        targetOffsetY = { -it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Error.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(Dimensions.CornerRadius)
                    ) {
                        Text(
                            text = if (loginState is LoginState.Error) (loginState as LoginState.Error).message else "",
                            modifier = Modifier.padding(Dimensions.SpacingMedium),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
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
                color = InactivoDeshabilitado
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