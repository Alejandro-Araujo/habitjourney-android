package com.alejandro.habitjourney.core.utils.formatter

import android.content.Context
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.Weekday
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime

/**
 * Objeto de utilidad para formatear fechas y horas para su visualización en la UI.
 */
object DateTimeFormatters {

    /**
     * Formatea una [LocalDate] en un formato relativo ("Hoy", "Mañana", "Ayer") o numérico.
     * @param date La fecha a formatear.
     * @param context El contexto para obtener los strings localizados.
     * @return Un String legible para el usuario.
     */
    fun formatDateRelatively(date: LocalDate, context: Context): String {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return when {
            date == today -> context.getString(R.string.date_today)
            date == today.plus(1, DateTimeUnit.DAY) -> context.getString(R.string.date_tomorrow)
            date == today.minus(1, DateTimeUnit.DAY) -> context.getString(R.string.date_yesterday)
            else -> formatDateShort(date)
        }
    }

    /**
     * Formatea una [LocalDate] a un formato corto numérico (ej: 15/06/25).
     * Utiliza el formato localizado del dispositivo.
     * @param date La fecha a formatear.
     * @return Un String con la fecha en formato corto.
     */
    fun formatDateLocalized(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        return date.toJavaLocalDate().format(formatter)
    }

    /**
     * Formatea una [LocalDate] a un formato numérico específico "dd/MM/yyyy".
     * @param date La fecha a formatear.
     * @return Un String con la fecha en formato "dd/MM/yyyy".
     */
    fun formatDateShort(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return date.toJavaLocalDate().format(formatter)
    }

    /**
     * Formatea una [LocalDateTime] a un formato de fecha y hora corto y localizado.
     * Se adapta a la configuración regional del usuario (ej: 15/06/25 12:06 o 6/15/25 12:06 PM).
     * @param dateTime La fecha y hora a formatear.
     * @return Un String con la fecha y hora localizadas.
     */
    fun formatDateTimeLocalized(dateTime: LocalDateTime): String {
        val javaDateTime = dateTime.toJavaLocalDateTime()
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        return javaDateTime.format(formatter)
    }
}

/**
 * Función de extensión para obtener el nombre legible y localizado de un [Weekday].
 * @param context El contexto de la aplicación para acceder a los recursos de strings.
 * @return Un [String] con el nombre del día de la semana (ej: "Lunes", "Martes").
 */
fun Weekday.displayName(context: Context): String {
    return when (this) {
        Weekday.MONDAY -> context.getString(R.string.weekday_monday)
        Weekday.TUESDAY -> context.getString(R.string.weekday_tuesday)
        Weekday.WEDNESDAY -> context.getString(R.string.weekday_wednesday)
        Weekday.THURSDAY -> context.getString(R.string.weekday_thursday)
        Weekday.FRIDAY -> context.getString(R.string.weekday_friday)
        Weekday.SATURDAY -> context.getString(R.string.weekday_saturday)
        Weekday.SUNDAY -> context.getString(R.string.weekday_sunday)
    }
}
