package com.alejandro.habitjourney.features.dashboard.di

import com.alejandro.habitjourney.features.dashboard.data.repository.DashboardRepositoryImpl
import com.alejandro.habitjourney.features.dashboard.domain.repository.DashboardRepository
import com.alejandro.habitjourney.features.dashboard.domain.usecase.CalculateProductivityScoreUseCase
import com.alejandro.habitjourney.features.dashboard.domain.usecase.GetDashboardDataUseCase
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import com.alejandro.habitjourney.features.note.domain.repository.NoteRepository
import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import com.alejandro.habitjourney.features.user.data.preferences.UserPreferences
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Dagger/Hilt para la funcionalidad del Dashboard.
 *
 * Responsabilidades:
 * - Proveer instancias de use cases del dashboard
 * - Configurar dependencias entre repositorios y use cases
 * - Gestionar inyección de dependencias para cálculos de productividad
 *
 * Scope: SingletonComponent para mantener instancias únicas durante
 * toda la vida de la aplicación.
 */
@Module
@InstallIn(SingletonComponent::class)
object DashboardModule {

    /**
     * Provee el use case para calcular el score de productividad.
     *
     * Este use case es independiente y puede ser utilizado por otros módulos
     * si es necesario. Maneja toda la lógica de cálculo de productividad
     * basada en hábitos y tareas completadas.
     *
     * @return Instancia singleton de CalculateProductivityScoreUseCase
     */
    @Provides
    @Singleton
    fun provideCalculateProductivityScoreUseCase(): CalculateProductivityScoreUseCase {
        return CalculateProductivityScoreUseCase()
    }

    /**
     * Provee el use case principal para obtener datos del dashboard.
     *
     * Este use case orquesta la obtención de todos los datos necesarios
     * para el dashboard, incluyendo:
     * - Datos de usuario, hábitos, tareas y notas
     * - Cálculo de estadísticas y métricas
     * - Integración con CalculateProductivityScoreUseCase
     *
     * Dependencias inyectadas:
     * - UserPreferences: Para obtener el usuario actual
     * - Repositorios: Para acceder a datos de diferentes features
     * - CalculateProductivityScoreUseCase: Para cálculo correcto de productividad
     *
     * @param userPreferences Preferencias del usuario actual
     * @param userRepository Repositorio de usuarios
     * @param habitRepository Repositorio de hábitos
     * @param taskRepository Repositorio de tareas
     * @param noteRepository Repositorio de notas
     * @param calculateProductivityScoreUseCase Use case para cálculo de productividad
     * @return Instancia singleton de GetDashboardDataUseCase
     */
    @Provides
    @Singleton
    fun provideGetDashboardDataUseCase(
        userPreferences: UserPreferences,
        userRepository: UserRepository,
        habitRepository: HabitRepository,
        taskRepository: TaskRepository,
        noteRepository: NoteRepository,
        calculateProductivityScoreUseCase: CalculateProductivityScoreUseCase
    ): GetDashboardDataUseCase {
        return GetDashboardDataUseCase(
            userPreferences = userPreferences,
            userRepository = userRepository,
            habitRepository = habitRepository,
            taskRepository = taskRepository,
            noteRepository = noteRepository,
            calculateProductivityScoreUseCase = calculateProductivityScoreUseCase
        )
    }

    /**
     * Provee la implementación del repositorio de dashboard.
     *
     * Este repositorio actúa como una capa de abstracción adicional
     * sobre el use case principal, útil para:
     * - Cacheo de datos si es necesario
     * - Transformaciones adicionales
     * - Abstracción para testing
     *
     * @param getDashboardDataUseCase Use case principal del dashboard
     * @return Instancia singleton del repositorio de dashboard
     */
    @Provides
    @Singleton
    fun provideDashboardRepository(
        getDashboardDataUseCase: GetDashboardDataUseCase
    ): DashboardRepository {
        return DashboardRepositoryImpl(getDashboardDataUseCase)
    }
}