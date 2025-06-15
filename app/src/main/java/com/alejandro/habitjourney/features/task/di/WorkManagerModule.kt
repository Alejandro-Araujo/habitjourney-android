package com.alejandro.habitjourney.features.task.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Dagger Hilt que proporciona la instancia de [WorkManager] a la aplicación.
 *
 * Este módulo asegura que [WorkManager] sea un singleton y esté disponible para inyección
 * en cualquier parte de la aplicación que lo requiera para la gestión de trabajos en segundo plano.
 */
@Module
@InstallIn(SingletonComponent::class)
object WorkManagerModule {

    /**
     * Provee una instancia singleton de [WorkManager].
     *
     * @param context El contexto de la aplicación, inyectado por Hilt.
     * @return Una instancia única de [WorkManager].
     */
    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager {
        return WorkManager.getInstance(context)
    }
}