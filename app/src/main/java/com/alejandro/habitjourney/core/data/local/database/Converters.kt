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
        return weekdays?.joinToString(",") { it.ordinal.toString() }?.let { ",$it," }
    }

    @TypeConverter
    fun toWeekdayList(data: String?): List<Weekday>? {
        return data?.trim(',')?.split(",")?.mapNotNull { it.toIntOrNull() }?.map { Weekday.entries[it] }
    }

    // ========== Fechas (LocalDate) ==========
    @TypeConverter
    fun localDateToEpochDay(date: LocalDate): Long = date.toEpochDays().toLong()

    @TypeConverter
    fun epochDayToLocalDate(epochDay: Long): LocalDate =
        LocalDate.fromEpochDays(epochDay.toInt())


    // ========== Priority Enum ==========
    @TypeConverter
    fun priorityToString(priority: com.alejandro.habitjourney.core.data.local.enums.Priority?): String? = priority?.name

    @TypeConverter
    fun stringToPriority(value: String?): com.alejandro.habitjourney.core.data.local.enums.Priority? =
        value?.let { com.alejandro.habitjourney.core.data.local.enums.Priority.valueOf(it) }

    // ========== LocalDateTime ==========
    @TypeConverter
    fun localDateTimeToString(dateTime: kotlinx.datetime.LocalDateTime?): String? = dateTime?.toString()

    @TypeConverter
    fun stringToLocalDateTime(value: String?): kotlinx.datetime.LocalDateTime? =
        value?.let { kotlinx.datetime.LocalDateTime.parse(it) }

}