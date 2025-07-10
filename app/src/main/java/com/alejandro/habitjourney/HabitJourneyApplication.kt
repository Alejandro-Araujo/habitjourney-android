package com.alejandro.habitjourney

import android.app.Application
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.media3.common.util.UnstableApi
import androidx.work.Configuration
import com.alejandro.habitjourney.core.utils.logging.AppLogger
import com.alejandro.habitjourney.features.settings.domain.repository.SettingsRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Clase principal de aplicación que configura el entorno global.
 *
 * Responsabilidades:
 * - Inicialización de Hilt
 * - Configuración de WorkManager
 * - Aplicar configuraciones de usuario (tema, idioma)
 * - Setup inicial de la app
 */
@HiltAndroidApp
class HabitJourneyApplication : Application(), Configuration.Provider {

    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var workerFactory: HiltWorkerFactory

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            try {
                val appSettings = settingsRepository.getAppSettings().first()

                // Aplicar Tema
                val themeMode = when (appSettings.theme) {
                    "light" -> AppCompatDelegate.MODE_NIGHT_NO
                    "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                AppCompatDelegate.setDefaultNightMode(themeMode)

                if (appSettings.language.isNotEmpty()) {
                    val localeList = LocaleListCompat.forLanguageTags(appSettings.language)
                    AppCompatDelegate.setApplicationLocales(localeList)
                }
            } catch (e: Exception) {
                AppLogger.e("Application", "Error applying user settings", e)
            }
        }
    }

    /**
     * Configuración de WorkManager con HiltWorkerFactory para DI.
     */
    override val workManagerConfiguration: Configuration
        @OptIn(UnstableApi::class)
        get() {
            AppLogger.d(
                "HabitJourneyApp",
                "Creating WorkManager config with HiltWorkerFactory: $workerFactory"
            )
            return Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .build()
        }
}
