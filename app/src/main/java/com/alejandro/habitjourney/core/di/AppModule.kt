package com.alejandro.habitjourney.core.di

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.database.AppDatabase
import com.alejandro.habitjourney.core.data.local.database.AppDatabase.Companion.DATABASE_NAME
import com.alejandro.habitjourney.core.data.remote.exception.ErrorHandler
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.core.utils.resources.ResourceProviderImpl
import com.alejandro.habitjourney.features.habit.data.dao.HabitDao
import com.alejandro.habitjourney.features.habit.data.dao.HabitLogDao
import com.alejandro.habitjourney.features.note.data.dao.NoteDao
import com.alejandro.habitjourney.features.task.data.dao.TaskDao
import com.alejandro.habitjourney.features.user.data.dao.UserDao
import com.alejandro.habitjourney.features.user.data.preferences.UserPreferences
import com.alejandro.habitjourney.features.user.domain.authentication.AuthenticationHandler
import com.alejandro.habitjourney.navigation.AuthFlowCoordinator
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Módulo principal de Dagger/Hilt que configura dependencias globales de la aplicación.
 *
 * Proporciona:
 * - Base de datos Room con DAOs
 * - Repositorios
 * - Utilidades globales (ResourceProvider, ErrorHandler)
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * CoroutineScope para operaciones a nivel de aplicación.
     * Usa SupervisorJob para que fallos de hijos no cancelen el scope.
     */
    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob())
    }

    /**
     * Base de datos Room configurada con foreign keys habilitadas.
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    db.execSQL("PRAGMA foreign_keys=ON")
                }
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideResourceProvider(
        @ApplicationContext context: Context
    ): ResourceProvider = ResourceProviderImpl(context)


    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return Firebase.auth
    }

    @Provides
    @Singleton
    fun provideCredentialManager(@ApplicationContext context: Context): CredentialManager {
        return CredentialManager.create(context)
    }

    @Provides
    @Singleton
    fun provideAuthenticationHandler(
        credentialManager: CredentialManager,
        authFlowCoordinator: AuthFlowCoordinator
    ): AuthenticationHandler {
        return AuthenticationHandler(credentialManager, authFlowCoordinator)
    }

    @Provides
    @Singleton
    fun provideGoogleWebClientId(@ApplicationContext context: Context): String {
        return context.getString(R.string.default_web_client_id)
    }

    @Provides
    @Singleton
    fun provideErrorHandler(@ApplicationContext context: Context): ErrorHandler {
        return ErrorHandler(context)
    }

    // DAOs
    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideHabitDao(database: AppDatabase): HabitDao = database.habitDao()

    @Provides
    @Singleton
    fun provideHabitLogDao(database: AppDatabase): HabitLogDao = database.habitLogDao()

    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase): TaskDao = database.taskDao()

    @Provides
    @Singleton
    fun provideNoteDao(database: AppDatabase): NoteDao = database.noteDao()

    @Provides
    @Singleton
    fun provideUserRepository(
        @ApplicationContext context: Context,
        firebaseAuth: FirebaseAuth,
        userDao: UserDao,
        userPreferences: UserPreferences,
        errorHandler: ErrorHandler
    ): com.alejandro.habitjourney.features.user.domain.repository.UserRepository {
        return com.alejandro.habitjourney.features.user.data.repository.UserRepositoryImpl(
            context,
            firebaseAuth,
            userDao,
            userPreferences,
            errorHandler
        )
    }
}