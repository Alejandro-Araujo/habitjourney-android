package com.alejandro.habitjourney

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.alejandro.habitjourney.navigation.HabitJourneyApp
import dagger.hilt.android.AndroidEntryPoint
import com.alejandro.habitjourney.core.presentation.ui.theme.HabitJourneyThemeWrapper
import com.alejandro.habitjourney.features.settings.domain.repository.SettingsRepository
import com.alejandro.habitjourney.features.user.domain.authentication.AuthenticationHandler
import javax.inject.Inject

/**
 * Activity principal que actúa como host de la navegación.
 * Configurada con Hilt y tema wrapper.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var authenticationHandler: AuthenticationHandler

    private lateinit var googleSignInLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar el ActivityResultLauncher para la autenticación
        setupAuthenticationLauncher()

        // Inicializar el AuthenticationHandler
        authenticationHandler.initialize(
            activity = this,
            lifecycleScope = lifecycleScope,
            googleSignInLauncher = googleSignInLauncher
        )

        // Configurar la UI
        setupUI()
    }

    private fun setupAuthenticationLauncher() {
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            authenticationHandler.handleActivityResult(
                activity = this,
                resultCode = result.resultCode,
                data = result.data
            )
        }
    }

    private fun setupUI() {
        setContent {
            HabitJourneyThemeWrapper {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HabitJourneyApp()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        authenticationHandler.cleanup()
    }
}