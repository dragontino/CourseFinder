package ru.coursefinder.app.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal object DateTimePatterns {
    const val DISPLAYABLE_DATE_PATTERN = "dd MMMM uuuu"
}


internal fun String.parseToDate(pattern: String? = null): LocalDate {
    val formatter = pattern
        ?.let { DateTimeFormatter.ofPattern(pattern, Locale.getDefault()) }
        ?: DateTimeFormatter.ISO_DATE_TIME
    return LocalDate.parse(this, formatter)
}


internal fun LocalDate.getDisplayableDateString(
    pattern: String = DateTimePatterns.DISPLAYABLE_DATE_PATTERN
): String {
    val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
    return this.format(formatter)
}