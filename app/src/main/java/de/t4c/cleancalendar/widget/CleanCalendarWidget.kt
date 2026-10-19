package de.t4c.cleancalendar.widget

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import de.t4c.cleancalendar.R
import de.t4c.cleancalendar.data.CalendarReader
import de.t4c.cleancalendar.data.SettingsRepository
import de.t4c.cleancalendar.model.CalendarEvent
import de.t4c.cleancalendar.model.WidgetSettings
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Glance AppWidget rendering the minimalist transparent calendar view.
 */
class CleanCalendarWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settingsRepo = SettingsRepository(context)
        val settings = settingsRepo.settingsFlow.first()
        val calendarReader = CalendarReader(context)
        val events = calendarReader.getUpcomingEvents(settings)

        provideContent {
            // Widget Container with transparent background
            val bgColor = Color(settings.backgroundColorHex)
            val textColor = Color(settings.textColorHex)

            // Open calendar app when clicking outside event rows
            val openCalendarIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("content://com.android.calendar/time/${System.currentTimeMillis()}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(bgColor)
                    .cornerRadius(16.dp)
                    .padding(settings.paddingDp.dp)
                    .clickable(actionStartActivity(openCalendarIntent))
            ) {
                if (events.isEmpty()) {
                    Box(
                        modifier = GlanceModifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = context.getString(R.string.no_events),
                            style = TextStyle(
                                color = ColorProvider(textColor.copy(alpha = 0.7f)),
                                fontSize = settings.fontSizeSp.sp
                            )
                        )
                    }
                } else {
                    WidgetContent(
                        context = context,
                        events = events,
                        settings = settings,
                        textColor = textColor
                    )
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun WidgetContent(
        context: Context,
        events: List<CalendarEvent>,
        settings: WidgetSettings,
        textColor: Color
    ) {
        val dateFormat = SimpleDateFormat(settings.dateFormatPattern, Locale.getDefault())
        val timeFormat = SimpleDateFormat(settings.timeFormatPattern, Locale.getDefault())

        val nowCal = Calendar.getInstance()
        val todayYear = nowCal.get(Calendar.YEAR)
        val todayDay = nowCal.get(Calendar.DAY_OF_YEAR)

        var lastDateKey = ""

        Column(modifier = GlanceModifier.fillMaxWidth()) {
            events.take(settings.maxItems).forEachIndexed { index, event ->
                val eventCal = Calendar.getInstance().apply { timeInMillis = event.startTime }
                val eventYear = eventCal.get(Calendar.YEAR)
                val eventDay = eventCal.get(Calendar.DAY_OF_YEAR)

                val dateLabel: String
                val isToday = (todayYear == eventYear && todayDay == eventDay)
                val isTomorrow = (todayYear == eventYear && todayDay + 1 == eventDay)

                if (settings.showTodayTomorrow && isToday) {
                    dateLabel = context.getString(R.string.today)
                } else if (settings.showTodayTomorrow && isTomorrow) {
                    dateLabel = context.getString(R.string.tomorrow)
                } else {
                    dateLabel = dateFormat.format(Date(event.startTime))
                }

                // Group events by date if displayDateOnce is true
                val showDate = !settings.displayDateOnce || (dateLabel != lastDateKey)
                lastDateKey = dateLabel

                val timeLabel = if (event.isAllDay) {
                    ""
                } else if (settings.showEndTime) {
                    "${timeFormat.format(Date(event.startTime))} - ${timeFormat.format(Date(event.endTime))}"
                } else {
                    timeFormat.format(Date(event.startTime))
                }

                val rowTextColor = if (settings.useCalendarColors && event.color != 0) {
                    Color(event.color)
                } else {
                    textColor
                }

                // Intent to view this specific event
                val eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.id)
                val viewEventIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = eventUri
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }

                if (index > 0) {
                    Spacer(modifier = GlanceModifier.height(4.dp))
                }

                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .clickable(actionStartActivity(viewEventIntent)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date Column
                    if (showDate) {
                        Text(
                            text = dateLabel,
                            style = TextStyle(
                                color = ColorProvider(rowTextColor),
                                fontSize = settings.fontSizeSp.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                    } else {
                        // Empty space alignment placeholder if grouping
                        Spacer(modifier = GlanceModifier.width(16.dp))
                    }

                    Spacer(modifier = GlanceModifier.width(8.dp))

                    // Time Column (or Event Title inline with time)
                    val fullTitle = if (timeLabel.isNotEmpty()) {
                        "$timeLabel  ${event.title}"
                    } else {
                        event.title
                    }

                    // Event Title
                    Text(
                        text = fullTitle,
                        style = TextStyle(
                            color = ColorProvider(rowTextColor),
                            fontSize = settings.fontSizeSp.sp
                        ),
                        maxLines = 1,
                        modifier = GlanceModifier.defaultWeight()
                    )
                }
            }
        }
    }
}
