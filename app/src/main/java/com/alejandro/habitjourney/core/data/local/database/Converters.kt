package com.alejandro.habitjourney.core.data.local.database

import androidx.room.TypeConverter
import com.alejandro.habitjourney.core.data.local.enums.HabitType
import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import com.alejandro.habitjourney.core.data.local.enums.Priority
import com.alejandro.habitjourney.core.data.local.enums.Weekday
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * Proporciona métodos de conversión de tipos para que Room pueda almacenar
 * tipos de datos complejos que no soporta de forma nativa (como Enums, Listas y Fechas).
 *
 * Room utilizará estos métodos automáticamente al leer o escribir en la base de datos.
 */
class Converters {
    // ========== Enums Individuales ==========

    /**
     * Convierte un [HabitType] a su representación en [String] para almacenarlo en la DB.
     * @param type El enum a convertir.
     * @return El nombre del enum como String.
     */
    @TypeConverter
    fun habitTypeToString(type: HabitType): String = type.name

    /**
     * Convierte un [String] de la base de datos de vuelta a un [HabitType].
     * @param value El String a convertir.
     * @return El enum [HabitType] correspondiente.
     */
    @TypeConverter
    fun stringToHabitType(value: String): HabitType = HabitType.valueOf(value)

    /**
     * Convierte un [LogStatus] a su representación en [String].
     * @param status El enum a convertir.
     * @return El nombre del enum como String.
     */
    @TypeConverter
    fun logStatusToString(status: LogStatus): String = status.name

    /**
     * Convierte un [String] de la base de datos de vuelta a un [LogStatus].
     * @param value El String a convertir.
     * @return El enum [LogStatus] correspondiente.
     */
    @TypeConverter
    fun stringToLogStatus(value: String): LogStatus = LogStatus.valueOf(value)

    /**
     * Convierte un enum de [Priority] (nulable) a [String].
     * @param priority El enum a convertir.
     * @return El nombre del enum como String, o null si el enum es null.
     */
    @TypeConverter
    fun priorityToString(priority: Priority?): String? = priority?.name

    /**
     * Convierte un [String] (nulable) de vuelta a un enum de [Priority].
     * @param value El String a convertir.
     * @return El enum [Priority] correspondiente, o null si el String es null.
     */
    @TypeConverter
    fun stringToPriority(value: String?): Priority? = value?.let { Priority.valueOf(it) }

    // ========== Listas de Enums ==========

    /**
     * Convierte una lista de [Weekday] a un único [String] para su almacenamiento.
     * La lista se almacena como una cadena de los valores ordinales de los enums,
     * separados por comas y envueltos por comas (ej: ",0,2,4,").
     *
     * @param weekdays La lista de días de la semana a convertir.
     * @return Un String que representa la lista, o null si la lista es null.
     */
    @TypeConverter
    fun fromWeekdayList(weekdays: List<Weekday>?): String? {
        return weekdays?.joinToString(",") { it.ordinal.toString() }?.let { ",$it," }
    }

    /**
     * Convierte un [String] de la base de datos de vuelta a una lista de [Weekday].
     * @param data El String con los ordinales de los días de la semana.
     * @return La lista de [Weekday] correspondiente, o null si el String es null.
     */
    @TypeConverter
    fun toWeekdayList(data: String?): List<Weekday>? {
        return data?.trim(',')?.split(",")?.mapNotNull { it.toIntOrNull() }?.map { Weekday.entries[it] }
    }

    // ========== Fechas (kotlinx.datetime) ==========

    /**
     * Convierte una [LocalDate] al número de días desde la época de Unix (epoch).
     * Se almacena como un [Long] para una mayor eficiencia.
     *
     * @param date La fecha a convertir.
     * @return El número de días desde la época como Long.
     */
    @TypeConverter
    fun localDateToEpochDay(date: LocalDate): Long = date.toEpochDays().toLong()

    /**
     * Convierte un número de días desde la época (epoch) de vuelta a una [LocalDate].
     * @param epochDay El número de días desde la época.
     * @return La [LocalDate] correspondiente.
     */
    @TypeConverter
    fun epochDayToLocalDate(epochDay: Long): LocalDate =
        LocalDate.fromEpochDays(epochDay.toInt())

    /**
     * Convierte una [LocalDateTime] (nulable) a su representación estándar en [String].
     * @param dateTime La fecha y hora a convertir.
     * @return El String ISO-8601, o null si el objeto es null.
     */
    @TypeConverter
    fun localDateTimeToString(dateTime: LocalDateTime?): String? = dateTime?.toString()

    /**
     * Convierte un [String] (nulable) de vuelta a una [LocalDateTime].
     * @param value El String a convertir.
     * @return La [LocalDateTime] correspondiente, o null si el String es null.
     */
    @TypeConverter
    fun stringToLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it) }
}
