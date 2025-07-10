package com.alejandro.habitjourney.features.habit.data.repository

import com.alejandro.habitjourney.features.habit.domain.model.Habit
import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import com.alejandro.habitjourney.features.habit.data.dao.HabitDao
import com.alejandro.habitjourney.features.habit.data.dao.HabitLogDao
import com.alejandro.habitjourney.features.habit.data.entity.HabitLogEntity
import com.alejandro.habitjourney.features.habit.data.mapper.HabitLocalMapper
import com.alejandro.habitjourney.features.habit.domain.model.HabitLog
import com.alejandro.habitjourney.features.habit.domain.model.HabitWithLogs
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * Implementación del [HabitRepository] que actúa como la única fuente de verdad
 * para los datos de hábitos.
 *
 * Esta clase coordina las operaciones entre los DAOs ([HabitDao], [HabitLogDao]) y el
 * dominio, aplicando la lógica de negocio necesaria y mapeando entre entidades de
 * base de datos y modelos de dominio.
 *
 * @property habitDao DAO para operaciones CRUD en [HabitEntity].
 * @property habitLogDao DAO para operaciones CRUD en [HabitLogEntity].
 * @property habitMapper Mapeador para convertir entre entidades y modelos de dominio.
 */
class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val habitMapper: HabitLocalMapper
) : HabitRepository {

    /**
     * Crea un nuevo hábito en la base de datos.
     * @param habit El modelo de dominio [Habit] a insertar.
     * @return El ID del hábito recién creado.
     */
    override suspend fun createHabit(habit: Habit): Long {
        return habitDao.insertHabit(habitMapper.habitDomainToEntity(habit))
    }

    /**
     * Actualiza un hábito existente.
     * @param habit El modelo de dominio [Habit] con los datos actualizados.
     */
    override suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habitMapper.habitDomainToEntity(habit))
    }

    /**
     * Marca un hábito como archivado, ocultándolo de las listas activas.
     * @param habitId El ID del hábito a archivar.
     */
    override suspend fun archiveHabit(habitId: Long) {
        habitDao.archiveHabit(habitId)
    }

    /**
     * Desarchiva un hábito, volviéndolo a mostrar en las listas activas.
     * @param habitId El ID del hábito a desarchivar.
     */
    override suspend fun unarchiveHabit(habitId: Long) {
        habitDao.unarchiveHabit(habitId)
    }

    /**
     * Obtiene un hábito activo por su ID.
     * @param habitId El ID del hábito a buscar.
     * @return El [Habit] correspondiente o null si no se encuentra o está archivado.
     */
    override suspend fun getHabitById(habitId: Long): Habit? {
        return habitDao.getHabitById(habitId)?.let { habitMapper.habitEntityToDomain(it) }
    }

    /**
     * Obtiene un hábito por su ID, sin importar si está archivado o no.
     * @param habitId El ID del hábito a buscar.
     * @return El [Habit] correspondiente o null si no existe.
     */
    override suspend fun getHabitByIdUnfiltered(habitId: Long): Habit? {
        return habitDao.getHabitByIdUnfiltered(habitId)?.let { habitMapper.habitEntityToDomain(it) }
    }

    /**
     * Obtiene una lista de todos los hábitos (activos y archivados) de un usuario.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite la lista de [Habit].
     */
    override fun getAllHabitsForUser(userId: String): Flow<List<Habit>> {
        return habitDao.getAllHabitsForUser(userId).map { entities ->
            habitMapper.habitEntityListToDomain(entities)
        }
    }

    /**
     * Obtiene una lista de solo los hábitos activos de un usuario.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite la lista de [Habit] activos.
     */
    override fun getActiveHabitsForUser(userId: String): Flow<List<Habit>> {
        return habitDao.getActiveHabitsForUser(userId).map { entities ->
            habitMapper.habitEntityListToDomain(entities)
        }
    }

    /**
     * Obtiene los hábitos programados para un día específico de la semana.
     * @param userId El ID del usuario.
     * @param weekdayIndex El índice del día de la semana (ej: 0 para Lunes).
     * @return Un [Flow] que emite la lista de [Habit] para ese día.
     */
    override fun getHabitsForDay(userId: String, weekdayIndex: Int): Flow<List<Habit>> {
        return habitDao.getHabitsForDay(userId, weekdayIndex).map { entities ->
            habitMapper.habitEntityListToDomain(entities)
        }
    }

    /**
     * Obtiene un hábito junto con todos sus registros de seguimiento.
     * @param habitId El ID del hábito.
     * @return Un [Flow] que emite un objeto [HabitWithLogs].
     */
    override fun getHabitWithLogs(habitId: Long): Flow<HabitWithLogs> {
        return habitDao.getHabitWithLogs(habitId).map { habitWithLogsEntity ->
            HabitWithLogs(
                habit = habitMapper.habitEntityToDomain(habitWithLogsEntity.habit),
                logs = habitMapper.habitLogEntityListToDomain(habitWithLogsEntity.logs)
            )
        }
    }

    /**
     * Cambia el estado de archivado de un hábito.
     * @param habitId El ID del hábito a modificar.
     * @param archive `true` para archivar, `false` para desarchivar.
     */
    override suspend fun toggleHabitArchived(habitId: Long, archive: Boolean) {
        if (archive) {
            habitDao.archiveHabit(habitId)
        } else {
            habitDao.unarchiveHabit(habitId)
        }
    }

    /**
     * Registra el progreso de un hábito para una fecha específica.
     * Si ya existe un registro para esa fecha, lo actualiza. Si no, crea uno nuevo.
     * @param habitId El ID del hábito.
     * @param date La fecha del registro.
     * @param value El valor del progreso (ej: 5 para un hábito de contar).
     * @param status El estado del registro (ej: COMPLETED, SKIPPED).
     */
    override suspend fun logHabitCompletion(habitId: Long, date: LocalDate, value: Float, status: LogStatus) {
        val existingLog = habitLogDao.getHabitLogForDate(habitId, date).first()

        if (existingLog != null) {
            val updatedLog = existingLog.copy(value = value, status = status)
            habitLogDao.updateLog(updatedLog)
        } else {
            val newLog = HabitLogEntity(
                habitId = habitId,
                date = date,
                value = value,
                status = status
            )
            habitLogDao.insertLog(newLog)
        }
    }

    /**
     * Registra o actualiza el progreso de un hábito a partir de un objeto [HabitLog].
     * Es una sobrecarga que simplifica el guardado desde el dominio.
     * @param habitLog El objeto de dominio [HabitLog] a guardar.
     */
    override suspend fun logHabitCompletion(habitLog: HabitLog) {
        val existingLog = habitLogDao.getHabitLogForDate(habitLog.habitId, habitLog.date).first()

        if (existingLog != null) {
            val updatedLog = existingLog.copy(
                value = habitLog.value,
                status = habitLog.status
            )
            habitLogDao.updateLog(updatedLog)
        } else {
            habitLogDao.insertLog(habitMapper.habitLogDomainToEntity(habitLog))
        }
    }

    /**
     * Actualiza un registro de hábito existente.
     * @param habitLog El modelo de dominio [HabitLog] con los datos actualizados.
     */
    override suspend fun updateHabitLog(habitLog: HabitLog) {
        habitLogDao.updateLog(habitMapper.habitLogDomainToEntity(habitLog))
    }

    /**
     * Obtiene todos los registros de un hábito dentro de un rango de fechas.
     * @param habitId El ID del hábito.
     * @param startDate La fecha de inicio del período.
     * @param endDate La fecha de fin del período.
     * @return Un [Flow] que emite la lista de [HabitLog].
     */
    override fun getLogsForPeriod(
        habitId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<HabitLog>> {
        return habitLogDao.getLogsForPeriod(habitId, startDate, endDate).map { entities ->
            habitMapper.habitLogEntityListToDomain(entities)
        }
    }

    /**
     * Obtiene el registro de un hábito para una fecha específica.
     * @param habitId El ID del hábito.
     * @param date La fecha a consultar.
     * @return Un [Flow] que emite el [HabitLog] o null si no hay registro para ese día.
     */
    override fun getLogForDate(habitId: Long, date: LocalDate): Flow<HabitLog?> {
        return habitLogDao.getHabitLogForDate(habitId, date).map { it?.let { habitMapper.habitLogEntityToDomain(it) } }
    }

    /**
     * Calcula la tasa de finalización de un hábito en un período.
     * @param habitId El ID del hábito.
     * @param startDate La fecha de inicio del período.
     * @param endDate La fecha de fin del período.
     * @return Un [Float] que representa el porcentaje de finalización.
     */
    override suspend fun getCompletionRate(
        habitId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Float {
        return habitLogDao.getCompletionRate(habitId, startDate, endDate)
    }

    /**
     * Obtiene los hábitos que deben realizarse hoy, junto con su conteo de completados
     * para la fecha de hoy.
     * @param userId El ID del usuario.
     * @param today La fecha de hoy.
     * @param weekdayIndex El índice del día de la semana de hoy.
     * @return Un [Flow] que emite una lista de pares, donde cada par contiene el
     * [Habit] y su conteo de completados hoy (Int).
     */
    override fun getHabitsDueTodayWithCompletionCount(
        userId: String,
        today: LocalDate,
        weekdayIndex: Int
    ): Flow<List<Pair<Habit, Int>>> {
        return habitDao.getHabitsDueTodayWithLogCounts(userId, today, weekdayIndex).map { list ->
            list.map { habitWithCompletionCount ->
                Pair(habitMapper.habitEntityToDomain(habitWithCompletionCount.habit), habitWithCompletionCount.currentCompletionCount)
            }
        }
    }
}
