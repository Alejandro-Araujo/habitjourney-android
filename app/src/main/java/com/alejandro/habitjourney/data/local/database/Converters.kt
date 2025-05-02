package com.alejandro.habitjourney.data.local.database

import androidx.room.TypeConverter
import com.alejandro.habitjourney.data.local.entity.enums.HabitType
import com.alejandro.habitjourney.data.local.entity.enums.LogStatus
import com.alejandro.habitjourney.data.local.entity.enums.Weekday
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

class Converters {
    // ========== Enums Individuales ==========
    @TypeConverter
    fun habitTypeToString(type: HabitType): String = type.name

    @TypeConverter
    fun stringToHabitType(value: String): HabitType = HabitType.valueOf(value)

    @TypeConverter
    fun logStatusToString(status: LogStatus): String = status.name

    @TypeConverter
    fun stringToLogStatus(value: String): LogStatus = LogStatus.valueOf(value)

    // ========== Listas de Enums (Para frequencyDays) ==========
    @TypeConverter
    fun weekdaysToString(list: List<Weekday>?): String? {
        return list?.joinToString(",") { it.name }
    }

    @TypeConverter
    fun stringToWeekdays(data: String?): List<Weekday>? {
        return data?.split(",")?.map { Weekday.valueOf(it) }
    }

    // ========== Fechas (LocalDate) ==========
    @TypeConverter
    fun localDateToEpochDay(date: LocalDate): Long = date.toEpochDays().toLong()

    @TypeConverter
    fun epochDayToLocalDate(epochDay: Long): LocalDate =
        LocalDate.fromEpochDays(epochDay.toInt())

    // Si necesitas fecha+hora
    @TypeConverter
    fun localDateToMillis(date: LocalDate): Long =
        date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

    @TypeConverter
    fun millisToLocalDate(millis: Long): LocalDate =
        Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
}