package de.t4c.cleancalendar.model

/**
 * Settings configuration for CleanCalendar widgets.
 * Matches all essential customizable features known from Smooth Calendar.
 */
data class WidgetSettings(
    // Appearance
    val backgroundAlpha: Int = 0, // 0 = 100% transparent by default
    val backgroundColorHex: Long = 0x00000000L,
    val textColorHex: Long = 0xFFFFFFFFL, // Crisp White
    val fontSizeSp: Int = 14,
    val showShadow: Boolean = true, // Text shadow for readability on light wallpapers
    val paddingDp: Int = 8,

    // Items and Filter
    val maxItems: Int = 4, // Default: 4 items as requested
    val daysSpan: Int = 14, // Lookahead 14 days
    val hideAllDay: Boolean = false,
    val hideMultiDay: Boolean = false,
    val removeOnEnd: Boolean = true,
    val ignoredCalendarIds: Set<Long> = emptySet(),

    // Formatting & Enhancements
    val showTodayTomorrow: Boolean = true,
    val displayDateOnce: Boolean = true,
    val showEndTime: Boolean = false,
    val useCalendarColors: Boolean = false,
    val dateFormatPattern: String = "EEE, d. MMM",
    val timeFormatPattern: String = "HH:mm"
)
