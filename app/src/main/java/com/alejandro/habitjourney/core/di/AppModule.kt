package com.alejandro.habitjourney.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alejandro.habitjourney.BuildConfig
import com.alejandro.habitjourney.core.data.local.database.AppDatabase
import com.alejandro.habitjourney.core.data.local.database.AppDatabase.Companion.DATABASE_NAME
import com.alejandro.habitjourney.core.data.remote.exception.ErrorHandler
import com.alejandro.habitjourney.core.data.remote.interceptor.AuthInterceptor
import com.alejandro.habitjourney.core.data.remote.interceptor.ErrorInterceptor
import com.alejandro.habitjourney.core.data.remote.interceptor.NetworkConnectionInterceptor
import com.alejandro.habitjourney.core.data.remote.network.RetrofitClient
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.core.utils.resources.ResourceProviderImpl
import com.alejandro.habitjourney.features.habit.data.dao.HabitDao
import com.alejandro.habitjourney.features.habit.data.dao.HabitLogDao
import com.alejandro.habitjourney.features.note.data.dao.NoteDao
import com.alejandro.habitjourney.features.task.data.dao.TaskDao
import com.alejandro.habitjourney.features.user.data.local.dao.UserDao
import com.alejandro.habitjourney.features.user.data.local.preferences.UserPreferences
import com.alejandro.habitjourney.features.user.data.remote.api.AuthApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Módulo principal de Dagger/Hilt que configura dependencias globales de la aplicación.
 *
 * Proporciona:
 * - Base de datos Room con DAOs
 * - Cliente HTTP con interceptors configurados
 * - Repositorios y servicios de red
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
    fun provideNetworkConnectionInterceptor(@ApplicationContext context: Context): NetworkConnectionInterceptor {
        return NetworkConnectionInterceptor(context)
    }

    @Provides
    @Singleton
    fun provideErrorInterceptor(): ErrorInterceptor {
        return ErrorInterceptor()
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(userPreferences: UserPreferences): AuthInterceptor {
        return AuthInterceptor(userPreferences)
    }

    @Provides
    @Singleton
    fun provideErrorHandler(@ApplicationContext context: Context): ErrorHandler {
        return ErrorHandler(context)
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * Cliente HTTP configurado con todos los interceptors en orden:
     * 1. NetworkConnection - Verifica conectividad
     * 2. Auth - Añade token de autorización
     * 3. Error - Convierte errores HTTP en excepciones
     * 4. Logging - Solo en debug builds
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        networkConnectionInterceptor: NetworkConnectionInterceptor,
        authInterceptor: AuthInterceptor,
        errorInterceptor: ErrorInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(networkConnectionInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(errorInterceptor)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(loggingInterceptor)
                }
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofitClient(okHttpClient: OkHttpClient): RetrofitClient {
        return RetrofitClient(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideRetrofit(retrofitClient: RetrofitClient): Retrofit {
        return retrofitClient.create()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
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
        authApi:AuthApi,
        userDao:UserDao,
        userPreferences: UserPreferences,
        errorHandler: ErrorHandler
    ): com.alejandro.habitjourney.features.user.domain.repository.UserRepository {
        return com.alejandro.habitjourney.features.user.data.repository.UserRepositoryImpl(
            context,
            authApi,
            userDao,
            userPreferences,
            errorHandler
        )
    }
}