package de.t4c.smoothcalendarng.data

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.CalendarContract
import de.t4c.smoothcalendarng.model.CalendarEvent
import de.t4c.smoothcalendarng.model.WidgetSettings
import java.util.concurrent.TimeUnit

/**
 * Service to read calendar events from Android CalendarContract provider.
 */
class CalendarReader(private val context: Context) {

    companion object {
        private val INSTANCE_PROJECTION = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.DISPLAY_COLOR,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.CALENDAR_ID
        )

        private const val PROJECTION_ID_INDEX = 0
        private const val PROJECTION_TITLE_INDEX = 1
        private const val PROJECTION_BEGIN_INDEX = 2
        private const val PROJECTION_END_INDEX = 3
        private const val PROJECTION_ALL_DAY_INDEX = 4
        private const val PROJECTION_COLOR_INDEX = 5
        private const val PROJECTION_LOCATION_INDEX = 6
        private const val PROJECTION_CALENDAR_ID_INDEX = 7
    }

    /**
     * Reads upcoming events according to widget settings.
     */
    fun getUpcomingEvents(settings: WidgetSettings): List<CalendarEvent> {
        val now = System.currentTimeMillis()
        val startWindow = if (settings.removeOnEnd) now else now - TimeUnit.HOURS.toMillis(12)
        val endWindow = now + TimeUnit.DAYS.toMillis(settings.daysSpan.toLong())

        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startWindow)
        ContentUris.appendId(builder, endWindow)

        val events = mutableListOf<CalendarEvent>()

        val cursor: Cursor? = try {
            context.contentResolver.query(
                builder.build(),
                INSTANCE_PROJECTION,
                null,
                null,
                CalendarContract.Instances.BEGIN + " ASC"
            )
        } catch (e: SecurityException) {
            // Permission not granted
            return emptyList()
        } catch (e: Exception) {
            return emptyList()
        }

        cursor?.use { c ->
            while (c.moveToNext()) {
                val calendarId = c.getLong(PROJECTION_CALENDAR_ID_INDEX)
                if (settings.ignoredCalendarIds.contains(calendarId)) {
                    continue
                }

                val isAllDay = c.getInt(PROJECTION_ALL_DAY_INDEX) != 0
                if (settings.hideAllDay && isAllDay) {
                    continue
                }

                val begin = c.getLong(PROJECTION_BEGIN_INDEX)
                val end = c.getLong(PROJECTION_END_INDEX)

                // Hide multi-day if configured (> 24 hours span)
                if (settings.hideMultiDay && (end - begin > TimeUnit.HOURS.toMillis(24))) {
                    continue
                }

                val id = c.getLong(PROJECTION_ID_INDEX)
                val title = c.getString(PROJECTION_TITLE_INDEX) ?: "Event"
                val color = c.getInt(PROJECTION_COLOR_INDEX)
                val location = c.getString(PROJECTION_LOCATION_INDEX)

                events.add(
                    CalendarEvent(
                        id = id,
                        title = title,
                        startTime = begin,
                        endTime = end,
                        isAllDay = isAllDay,
                        color = color,
                        location = location,
                        calendarId = calendarId
                    )
                )

                if (events.size >= settings.maxItems) {
                    break
                }
            }
        }

        return events
    }
}
