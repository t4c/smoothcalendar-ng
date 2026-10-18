package de.t4c.cleancalendar.model

/**
 * Clean data model representing a calendar event retrieved from the Android CalendarContract.
 */
data class CalendarEvent(
    val id: Long,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val isAllDay: Boolean,
    val color: Int,
    val location: String?,
    val calendarId: Long
)
