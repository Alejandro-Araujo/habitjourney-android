package com.alejandro.habitjourney.core.data.local.database

import androidx.room.TypeConverter
import com.alejandro.habitjourney.core.data.local.enums.HabitType
import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import com.alejandro.habitjourney.core.data.local.enums.Weekday
import kotlinx.datetime.LocalDate

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

    // ========== Listas de Enums ==========

    @TypeConverter
    fun fromWeekdayList(weekdays: List<Weekday>?): String? {
        return weekdays?.joinToString(",") { it.ordinal.toString() }
    }

    @TypeConverter
    fun toWeekdayList(data: String?): List<Weekday>? {
        return data?.split(",")?.map { Weekday.entries[it.toInt()] }
    }

    // ========== Fechas (LocalDate) ==========
    @TypeConverter
    fun localDateToEpochDay(date: LocalDate): Long = date.toEpochDays().toLong()

    @TypeConverter
    fun epochDayToLocalDate(epochDay: Long): LocalDate =
        LocalDate.fromEpochDays(epochDay.toInt())

}