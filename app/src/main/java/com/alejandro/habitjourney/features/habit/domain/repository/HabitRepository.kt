package com.alejandro.habitjourney.features.habit.domain.repository

import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import com.alejandro.habitjourney.features.habit.domain.model.Habit
import com.alejandro.habitjourney.features.habit.domain.model.HabitLog
import com.alejandro.habitjourney.features.habit.domain.model.HabitWithLogs
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Interfaz que define el contrato para la gestión de datos de hábitos.
 *
 * Actúa como una abstracción sobre la capa de datos, permitiendo que el dominio
 * interactúe con los datos de los hábitos sin conocer los detalles de implementación
 * (ej: si vienen de una base de datos local o de una API remota).
 */
interface HabitRepository {
    /**
     * Crea un nuevo hábito.
     * @param habit El [Habit] a crear.
     * @return El ID del hábito recién creado.
     */
    suspend fun createHabit(habit: Habit): Long

    /**
     * Actualiza un hábito existente.
     * @param habit El [Habit] con los datos actualizados.
     */
    suspend fun updateHabit(habit: Habit)

    /**
     * Marca un hábito como archivado.
     * @param habitId El ID del hábito a archivar.
     */
    suspend fun archiveHabit(habitId: Long)

    /**
     * Quita el estado de archivado de un hábito.
     * @param habitId El ID del hábito a desarchivar.
     */
    suspend fun unarchiveHabit(habitId: Long)

    /**
     * Obtiene un hábito activo por su ID.
     * @param habitId El ID del hábito.
     * @return El [Habit] correspondiente si es activo, o null.
     */
    suspend fun getHabitById(habitId: Long): Habit?

    /**
     * Obtiene un hábito por su ID, independientemente de si está archivado.
     * @param habitId El ID del hábito.
     * @return El [Habit] correspondiente, o null si no existe.
     */
    suspend fun getHabitByIdUnfiltered(habitId: Long): Habit?

    /**
     * Obtiene todos los hábitos (activos y archivados) de un usuario.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite la lista de todos los [Habit].
     */
    fun getAllHabitsForUser(userId: Long): Flow<List<Habit>>

    /**
     * Obtiene todos los hábitos activos de un usuario.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite la lista de [Habit] activos.
     */
    fun getActiveHabitsForUser(userId: Long): Flow<List<Habit>>

    /**
     * Obtiene los hábitos programados para un día específico de la semana.
     * @param userId El ID del usuario.
     * @param weekdayIndex El índice del día de la semana.
     * @return Un [Flow] que emite la lista de [Habit] para ese día.
     */
    fun getHabitsForDay(userId: Long, weekdayIndex: Int): Flow<List<Habit>>

    /**
     * Obtiene un hábito junto con todos sus registros de seguimiento.
     * @param habitId El ID del hábito.
     * @return Un [Flow] que emite el [HabitWithLogs] correspondiente.
     */
    fun getHabitWithLogs(habitId: Long): Flow<HabitWithLogs>

    /**
     * Obtiene el registro de un hábito para una fecha específica.
     * @param habitId El ID del hábito.
     * @param date La fecha del registro a obtener.
     * @return Un [Flow] que emite el [HabitLog] o null si no existe.
     */
    fun getLogForDate(habitId: Long, date: LocalDate): Flow<HabitLog?>

    /**
     * Cambia el estado de archivado de un hábito.
     * @param habitId El ID del hábito.
     * @param archive `true` para archivar, `false` para desarchivar.
     */
    suspend fun toggleHabitArchived(habitId: Long, archive: Boolean)

    /**
     * Registra el progreso de un hábito para una fecha, valor y estado específicos.
     * @param habitId El ID del hábito.
     * @param date La fecha del registro.
     * @param value El valor del progreso.
     * @param status El estado del registro.
     */
    suspend fun logHabitCompletion(habitId: Long, date: LocalDate, value: Float, status: LogStatus)

    /**
     * Registra o actualiza el progreso de un hábito usando un objeto [HabitLog].
     * @param habitLog El registro a guardar.
     */
    suspend fun logHabitCompletion(habitLog: HabitLog)

    /**
     * Actualiza un registro de hábito existente.
     * @param habitLog El registro con los datos actualizados.
     */
    suspend fun updateHabitLog(habitLog: HabitLog)

    /**
     * Obtiene los registros de un hábito para un período de tiempo.
     * @param habitId El ID del hábito.
     * @param startDate La fecha de inicio del período.
     * @param endDate La fecha de fin del período.
     * @return Un [Flow] que emite la lista de [HabitLog].
     */
    fun getLogsForPeriod(habitId: Long, startDate: LocalDate, endDate: LocalDate): Flow<List<HabitLog>>

    /**
     * Calcula la tasa de finalización de un hábito en un período.
     * @param habitId El ID del hábito.
     * @param startDate La fecha de inicio.
     * @param endDate La fecha de fin.
     * @return Un [Float] que representa el porcentaje de finalización.
     */
    suspend fun getCompletionRate(habitId: Long, startDate: LocalDate, endDate: LocalDate): Float

    /**
     * Obtiene los hábitos que tocan hoy, junto con su conteo de completados para hoy.
     * @param userId El ID del usuario.
     * @param today La fecha de hoy.
     * @param weekdayIndex El índice del día de la semana de hoy.
     * @return Un [Flow] que emite una lista de pares [Habit] y su conteo (Int).
     */
    fun getHabitsDueTodayWithCompletionCount(userId: Long, today: LocalDate, weekdayIndex: Int): Flow<List<Pair<Habit, Int>>>
}
