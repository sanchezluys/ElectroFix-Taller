package com.example.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DateFilterOption(val label: String, val shortLabel: String) {
    ALL("Todas", "Todas"),
    TODAY("Hoy", "Hoy"),
    LAST_7_DAYS("Últimos 7 días", "7 días"),
    THIS_MONTH("Este mes", "Este mes"),
    CUSTOM("Personalizado", "Personalizado");

    fun getRange(customStart: Long?, customEnd: Long?): Pair<Long?, Long?> {
        val cal = Calendar.getInstance()
        return when (this) {
            ALL -> Pair(null, null)
            TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            LAST_7_DAYS -> {
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                cal.add(Calendar.DAY_OF_YEAR, -6)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                Pair(start, end)
            }
            THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            CUSTOM -> {
                val start = customStart?.let {
                    val cStart = Calendar.getInstance().apply { timeInMillis = it }
                    cStart.set(Calendar.HOUR_OF_DAY, 0)
                    cStart.set(Calendar.MINUTE, 0)
                    cStart.set(Calendar.SECOND, 0)
                    cStart.set(Calendar.MILLISECOND, 0)
                    cStart.timeInMillis
                }
                val end = customEnd?.let {
                    val cEnd = Calendar.getInstance().apply { timeInMillis = it }
                    cEnd.set(Calendar.HOUR_OF_DAY, 23)
                    cEnd.set(Calendar.MINUTE, 59)
                    cEnd.set(Calendar.SECOND, 59)
                    cEnd.set(Calendar.MILLISECOND, 999)
                    cEnd.timeInMillis
                }
                Pair(start, end)
            }
        }
    }

    companion object {
        fun formatCustomRange(start: Long?, end: Long?): String {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return when {
                start != null && end != null -> "${sdf.format(Date(start))} - ${sdf.format(Date(end))}"
                start != null -> "Desde ${sdf.format(Date(start))}"
                end != null -> "Hasta ${sdf.format(Date(end))}"
                else -> "Personalizado"
            }
        }
    }
}
