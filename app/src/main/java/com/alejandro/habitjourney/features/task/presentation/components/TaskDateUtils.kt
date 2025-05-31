package com.alejandro.habitjourney.features.task.presentation.components

import kotlinx.datetime.*
import kotlinx.datetime.format.char

object TaskDateUtils {
    fun formatDate(date: LocalDate): String {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return when {
            date == today -> "Hoy"
            date == today.plus(1, DateTimeUnit.DAY) -> "Mañana"
            date == today.minus(1, DateTimeUnit.DAY) -> "Ayer"
            else -> {
                val formatter = LocalDate.Format {
                    dayOfMonth()
                    char('/')
                    monthNumber()
                    char('/')
                    year()
                }
                date.format(formatter)
            }
        }
    }

    fun formatDateTime(dateTime: LocalDateTime): String {
        val formatter = LocalDateTime.Format {
            dayOfMonth()
            char('/')
            monthNumber()
            char('/')
            year()
            char(' ')
            hour()
            char(':')
            minute()
        }
        return dateTime.format(formatter)
    }

    fun formatDateForDisplay(date: LocalDate): String = formatDate(date)
    fun formatDateTimeForDisplay(dateTime: LocalDateTime): String = formatDateTime(dateTime)
}