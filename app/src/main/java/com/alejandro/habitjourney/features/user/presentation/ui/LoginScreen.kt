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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.features.user.presentation.state.LoginState
import com.alejandro.habitjourney.features.user.presentation.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Header
        Text(
            text = stringResource(R.string.welcome_back_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.login_subtitle),
            fontSize = 16.sp,
            color = Color(0xFFA0AEC0),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Formulario
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Campo Email
                OutlinedTextField(
                    value = email,
                    onValueChange = viewModel::onEmailChanged,
                    label = { Text(stringResource(R.string.email_label)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = stringResource(R.string.content_description_email_icon),
                            tint = Color(0xFF4299E1)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true,
                    isError = emailError != null,
                    supportingText = {
                        AnimatedVisibility(
                            visible = emailError != null,
                            enter = slideInVertically(
                                initialOffsetY = { -it },
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300)),
                            exit = slideOutVertically(
                                targetOffsetY = { -it },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        ) {
                            Text(
                                text = emailError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4299E1),
                        focusedLabelColor = Color(0xFF4299E1),
                        cursorColor = Color(0xFF4299E1),
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        errorLabelColor = MaterialTheme.colorScheme.error
                    )
                )

                // Campo Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = { Text(stringResource(R.string.password_label)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = stringResource(R.string.content_description_lock_icon),
                            tint = Color(0xFF4299E1)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = viewModel::togglePasswordVisibility) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = if (isPasswordVisible) stringResource(R.string.hide_password_content_description)
                                else stringResource(R.string.show_password_content_description),
                                tint = Color(0xFFA0AEC0)
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
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
                    singleLine = true,
                    isError = passwordError != null,
                    supportingText = {
                        AnimatedVisibility(
                            visible = passwordError != null,
                            enter = slideInVertically(
                                initialOffsetY = { -it },
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300)),
                            exit = slideOutVertically(
                                targetOffsetY = { -it },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        ) {
                            Text(
                                text = passwordError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4299E1),
                        focusedLabelColor = Color(0xFF4299E1),
                        cursorColor = Color(0xFF4299E1),
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        errorLabelColor = MaterialTheme.colorScheme.error
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Botón de Login
                Button(
                    onClick = viewModel::login,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = loginState !is LoginState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4299E1),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    AnimatedContent(
                        targetState = loginState is LoginState.Loading,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith
                                    fadeOut(animationSpec = tween(300))
                        }
                    ) { isLoading ->
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.login_button_text),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

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
                            containerColor = Color(0xFFFED7D7)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (loginState is LoginState.Error) (loginState as LoginState.Error).message else "",
                            modifier = Modifier.padding(12.dp),
                            color = Color(0xFFC53030),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Enlace a registro
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.no_account_question),
                color = Color(0xFFA0AEC0),
                fontSize = 14.sp
            )
            TextButton(
                onClick = onNavigateToRegister,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF4299E1)
                )
            ) {
                Text(
                    text = stringResource(R.string.register_link_text),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}