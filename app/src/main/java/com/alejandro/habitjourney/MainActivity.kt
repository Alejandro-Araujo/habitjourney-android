package com.alejandro.habitjourney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable // Asegúrate de importar esto si lo usas en un @Composable
import androidx.compose.ui.Modifier
import com.alejandro.habitjourney.core.presentation.ui.theme.HabitJourneyTheme
import com.alejandro.habitjourney.navigation.HabitJourneyApp // Importar HabitJourneyApp
import dagger.hilt.android.AndroidEntryPoint
import com.alejandro.habitjourney.core.presentation.ui.theme.HabitJourneyTheme


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            HabitJourneyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HabitJourneyApp()
                }
            }
        }
    }
}