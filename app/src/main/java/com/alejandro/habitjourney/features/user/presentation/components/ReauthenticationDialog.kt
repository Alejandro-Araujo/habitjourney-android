package com.alejandro.habitjourney.features.user.presentation.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButton
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyButtonType
import com.alejandro.habitjourney.core.presentation.ui.components.HabitJourneyTextField
import com.alejandro.habitjourney.core.presentation.ui.theme.Dimensions
import com.alejandro.habitjourney.features.user.presentation.state.ReauthenticationState
import com.alejandro.habitjourney.features.user.presentation.state.ReauthenticationType

/**
 * Diálogo reutilizable para reautenticación de usuarios.
 * Soporta tanto autenticación por email/password como Google.
 */
@Composable
fun ReauthenticationDialog(
    modifier: Modifier = Modifier,
    state: ReauthenticationState,
    onPasswordChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onGoogleSignIn: (() -> Unit)? = null,
) {
    if (!state.showDialog) return

    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.reauthenticate_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
            ) {
                Text(
                    text = stringResource(R.string.reauthenticate_message),
                    style = MaterialTheme.typography.bodyMedium
                )

                when (state.type) {
                    ReauthenticationType.EMAIL_PASSWORD -> {
                        HabitJourneyTextField(
                            value = state.passwordInput,
                            onValueChange = onPasswordChange,
                            label = stringResource(R.string.label_current_password),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                val image = if (passwordVisible) {
                                    Icons.Filled.VisibilityOff
                                } else {
                                    Icons.Filled.Visibility
                                }
                                val description = if (passwordVisible) {
                                    stringResource(R.string.hide_password_content_description)
                                } else {
                                    stringResource(R.string.show_password_content_description)
                                }
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, contentDescription = description)
                                }
                            },
                            isError = state.passwordError != null,
                            helperText = state.passwordError,
                            enabled = !state.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    ReauthenticationType.GOOGLE -> {
                        Text(
                            text = stringResource(R.string.reauthenticate_google_instructions),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (onGoogleSignIn != null) {
                            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
                            HabitJourneyButton(
                                text = stringResource(R.string.sign_in_with_google_text),
                                onClick = onGoogleSignIn,
                                enabled = !state.isLoading,
                                type = HabitJourneyButtonType.SECONDARY,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    null -> {
                        Text(
                            text = stringResource(R.string.error_generic_reauth_issue),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            HabitJourneyButton(
                text = stringResource(R.string.action_confirm),
                onClick = onConfirm,
                isLoading = state.isLoading,
                enabled = !state.isLoading && when (state.type) {
                    ReauthenticationType.EMAIL_PASSWORD -> state.passwordInput.isNotBlank()
                    ReauthenticationType.GOOGLE -> true
                    null -> false
                },
                type = HabitJourneyButtonType.PRIMARY
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !state.isLoading
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        modifier = modifier
    )
}
