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
import androidx.compose.material.icons.filled.Person
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
import com.alejandro.habitjourney.features.user.presentation.state.RegisterState
import com.alejandro.habitjourney.features.user.presentation.viewmodel.RegisterViewModel

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
import com.alejandro.habitjourney.core.presentation.ui.theme.Exito // Para el mensaje de éxito

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
    val focusManager = LocalFocusManager.current

    // Manejo del estado de éxito
    LaunchedEffect(registerState) {
        if (registerState is RegisterState.Success) {
            onRegisterSuccess()
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BaseClara)
            .verticalScroll(rememberScrollState())
            .padding(Dimensions.SpacingLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(Dimensions.ButtonHeight))

        // Header
        Text(
            text = stringResource(R.string.create_account_title),
            style = MaterialTheme.typography.displayLarge,
            color = BaseOscura,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

        Text(
            text = stringResource(R.string.register_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = InactivoDeshabilitado,
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
                // Campo Nombre
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
                            tint = AcentoInformativo
                        )
                    }
                )

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
                            tint = AcentoInformativo
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
                            tint = AcentoInformativo
                        )
                    },
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

                // Campo Confirmar Contraseña
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
                            tint = AcentoInformativo
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = viewModel::toggleConfirmPasswordVisibility) {
                            Icon(
                                imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = if (isConfirmPasswordVisible) stringResource(R.string.hide_password_content_description)
                                else stringResource(R.string.show_password_content_description),
                                tint = InactivoDeshabilitado
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

                // Botón de Registro
                HabitJourneyButton(
                    text = stringResource(R.string.register_button_text),
                    onClick = viewModel::register,
                    type = HabitJourneyButtonType.PRIMARY,
                    enabled = registerState !is RegisterState.Loading,
                    isLoading = registerState is RegisterState.Loading
                )

                // Mensaje de error general
                AnimatedVisibility(
                    visible = registerState is RegisterState.Error,
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
                            text = (registerState as? RegisterState.Error)?.message ?: stringResource(R.string.error_unknown),
                            modifier = Modifier.padding(Dimensions.SpacingMedium),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Error,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Mensaje de éxito
                AnimatedVisibility(
                    visible = registerState is RegisterState.Success,
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
                            containerColor = Exito.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(Dimensions.CornerRadius)
                    ) {
                        Text(
                            text = stringResource(R.string.account_created_success),
                            modifier = Modifier.padding(Dimensions.SpacingMedium),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Exito,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        // Términos y condiciones
        Text(
            text = stringResource(R.string.terms_and_privacy_text),
            style = MaterialTheme.typography.bodySmall,
            color = InactivoDeshabilitado,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Dimensions.SpacingMedium)
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

        // Enlace a login
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.already_have_account_question),
                style = MaterialTheme.typography.bodyMedium,
                color = InactivoDeshabilitado
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