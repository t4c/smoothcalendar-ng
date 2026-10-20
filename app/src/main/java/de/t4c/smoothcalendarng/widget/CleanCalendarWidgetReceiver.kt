package de.t4c.smoothcalendarng.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Standard Glance AppWidgetReceiver delivering widget broadcast updates.
 */
class CleanCalendarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CleanCalendarWidget()
}
