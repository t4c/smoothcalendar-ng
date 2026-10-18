package de.t4c.cleancalendar.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.t4c.cleancalendar.model.WidgetSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "clean_calendar_settings")

/**
 * DataStore repository to persist and observe widget preferences.
 */
class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val BACKGROUND_ALPHA = intPreferencesKey("background_alpha")
        val TEXT_COLOR_HEX = longPreferencesKey("text_color_hex")
        val FONT_SIZE_SP = intPreferencesKey("font_size_sp")
        val SHOW_SHADOW = booleanPreferencesKey("show_shadow")
        val MAX_ITEMS = intPreferencesKey("max_items")
        val DAYS_SPAN = intPreferencesKey("days_span")
        val HIDE_ALL_DAY = booleanPreferencesKey("hide_all_day")
        val HIDE_MULTI_DAY = booleanPreferencesKey("hide_multi_day")
        val REMOVE_ON_END = booleanPreferencesKey("remove_on_end")
        val SHOW_TODAY_TOMORROW = booleanPreferencesKey("show_today_tomorrow")
        val DISPLAY_DATE_ONCE = booleanPreferencesKey("display_date_once")
        val SHOW_END_TIME = booleanPreferencesKey("show_end_time")
        val USE_CALENDAR_COLORS = booleanPreferencesKey("use_calendar_colors")
        val DATE_FORMAT = stringPreferencesKey("date_format")
        val TIME_FORMAT = stringPreferencesKey("time_format")
        val IGNORED_CALENDARS = stringSetPreferencesKey("ignored_calendars")
    }

    val settingsFlow: Flow<WidgetSettings> = context.dataStore.data.map { prefs ->
        val ignoredSet = prefs[PreferencesKeys.IGNORED_CALENDARS]?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
        WidgetSettings(
            backgroundAlpha = prefs[PreferencesKeys.BACKGROUND_ALPHA] ?: 0,
            textColorHex = prefs[PreferencesKeys.TEXT_COLOR_HEX] ?: 0xFFFFFFFFL,
            fontSizeSp = prefs[PreferencesKeys.FONT_SIZE_SP] ?: 14,
            showShadow = prefs[PreferencesKeys.SHOW_SHADOW] ?: true,
            maxItems = prefs[PreferencesKeys.MAX_ITEMS] ?: 4,
            daysSpan = prefs[PreferencesKeys.DAYS_SPAN] ?: 14,
            hideAllDay = prefs[PreferencesKeys.HIDE_ALL_DAY] ?: false,
            hideMultiDay = prefs[PreferencesKeys.HIDE_MULTI_DAY] ?: false,
            removeOnEnd = prefs[PreferencesKeys.REMOVE_ON_END] ?: true,
            showTodayTomorrow = prefs[PreferencesKeys.SHOW_TODAY_TOMORROW] ?: true,
            displayDateOnce = prefs[PreferencesKeys.DISPLAY_DATE_ONCE] ?: true,
            showEndTime = prefs[PreferencesKeys.SHOW_END_TIME] ?: false,
            useCalendarColors = prefs[PreferencesKeys.USE_CALENDAR_COLORS] ?: false,
            dateFormatPattern = prefs[PreferencesKeys.DATE_FORMAT] ?: "EEE, d. MMM",
            timeFormatPattern = prefs[PreferencesKeys.TIME_FORMAT] ?: "HH:mm",
            ignoredCalendarIds = ignoredSet
        )
    }

    suspend fun updateSettings(settings: WidgetSettings) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.BACKGROUND_ALPHA] = settings.backgroundAlpha
            prefs[PreferencesKeys.TEXT_COLOR_HEX] = settings.textColorHex
            prefs[PreferencesKeys.FONT_SIZE_SP] = settings.fontSizeSp
            prefs[PreferencesKeys.SHOW_SHADOW] = settings.showShadow
            prefs[PreferencesKeys.MAX_ITEMS] = settings.maxItems
            prefs[PreferencesKeys.DAYS_SPAN] = settings.daysSpan
            prefs[PreferencesKeys.HIDE_ALL_DAY] = settings.hideAllDay
            prefs[PreferencesKeys.HIDE_MULTI_DAY] = settings.hideMultiDay
            prefs[PreferencesKeys.REMOVE_ON_END] = settings.removeOnEnd
            prefs[PreferencesKeys.SHOW_TODAY_TOMORROW] = settings.showTodayTomorrow
            prefs[PreferencesKeys.DISPLAY_DATE_ONCE] = settings.displayDateOnce
            prefs[PreferencesKeys.SHOW_END_TIME] = settings.showEndTime
            prefs[PreferencesKeys.USE_CALENDAR_COLORS] = settings.useCalendarColors
            prefs[PreferencesKeys.DATE_FORMAT] = settings.dateFormatPattern
            prefs[PreferencesKeys.TIME_FORMAT] = settings.timeFormatPattern
            prefs[PreferencesKeys.IGNORED_CALENDARS] = settings.ignoredCalendarIds.map { it.toString() }.toSet()
        }
    }
}
