package de.t4c.cleancalendar.ui

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import de.t4c.cleancalendar.widget.CleanCalendarWidgetReceiver
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.updateAll
import de.t4c.cleancalendar.data.SettingsRepository
import de.t4c.cleancalendar.model.WidgetSettings
import de.t4c.cleancalendar.widget.CleanCalendarWidget
import kotlinx.coroutines.launch

/**
 * MainActivity providing in-app configuration matching Smooth Calendar settings.
 */
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsRepo = SettingsRepository(applicationContext)

        setContent {
            val darkTheme = isSystemInDarkTheme()
            val colorScheme = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    val context = applicationContext
                    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                }
                darkTheme -> darkColorScheme()
                else -> lightColorScheme()
            }

            MaterialTheme(colorScheme = colorScheme) {
                val settings by settingsRepo.settingsFlow.collectAsState(initial = WidgetSettings())
                val coroutineScope = rememberCoroutineScope()

                var hasPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.READ_CALENDAR
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    hasPermission = isGranted
                    if (isGranted) {
                        coroutineScope.launch {
                            CleanCalendarWidget().updateAll(applicationContext)
                        }
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("CleanCalendar Settings") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (!hasPermission) {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Calendar permission is required for CleanCalendar widget.")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(onClick = {
                                        permissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                                    }) {
                                        Text("Grant Calendar Permission")
                                    }
                                }
                            }
                        }

                        // Max items (Number Input)
                        var maxItemsText by remember(settings.maxItems) { mutableStateOf(settings.maxItems.toString()) }
                        SettingNumberInput(
                            title = "Visible Events",
                            subtitle = "Number of events to display (1 - 50)",
                            value = maxItemsText,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() }
                                maxItemsText = filtered
                                val parsed = filtered.toIntOrNull()
                                if (parsed != null && parsed in 1..50) {
                                    val updated = settings.copy(maxItems = parsed)
                                    coroutineScope.launch {
                                        settingsRepo.updateSettings(updated)
                                        CleanCalendarWidget().updateAll(applicationContext)
                                    }
                                }
                            }
                        )

                        // Font size (Number Input)
                        var fontSizeText by remember(settings.fontSizeSp) { mutableStateOf(settings.fontSizeSp.toString()) }
                        SettingNumberInput(
                            title = "Font Size",
                            subtitle = "Event text size in sp (8 - 36)",
                            value = fontSizeText,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() }
                                fontSizeText = filtered
                                val parsed = filtered.toIntOrNull()
                                if (parsed != null && parsed in 8..36) {
                                    val updated = settings.copy(fontSizeSp = parsed)
                                    coroutineScope.launch {
                                        settingsRepo.updateSettings(updated)
                                        CleanCalendarWidget().updateAll(applicationContext)
                                    }
                                }
                            }
                        )

                        HorizontalDivider()

                        // Display Date Once (Grouping)
                        SettingToggle(
                            title = "Group by Date",
                            subtitle = "Show date label only once for multiple events on the same day",
                            checked = settings.displayDateOnce,
                            onCheckedChange = { checked ->
                                val updated = settings.copy(displayDateOnce = checked)
                                coroutineScope.launch {
                                    settingsRepo.updateSettings(updated)
                                    CleanCalendarWidget().updateAll(applicationContext)
                                }
                            }
                        )

                        // Show Today / Tomorrow
                        SettingToggle(
                            title = "Show 'Today' / 'Tomorrow'",
                            subtitle = "Replace date string with relative labels when applicable",
                            checked = settings.showTodayTomorrow,
                            onCheckedChange = { checked ->
                                val updated = settings.copy(showTodayTomorrow = checked)
                                coroutineScope.launch {
                                    settingsRepo.updateSettings(updated)
                                    CleanCalendarWidget().updateAll(applicationContext)
                                }
                            }
                        )

                        // Hide All-Day Events
                        SettingToggle(
                            title = "Hide All-Day Events",
                            subtitle = "Do not show events scheduled for the entire day",
                            checked = settings.hideAllDay,
                            onCheckedChange = { checked ->
                                val updated = settings.copy(hideAllDay = checked)
                                coroutineScope.launch {
                                    settingsRepo.updateSettings(updated)
                                    CleanCalendarWidget().updateAll(applicationContext)
                                }
                            }
                        )

                        // Hide Multi-Day Events
                        SettingToggle(
                            title = "Hide Multi-Day Events",
                            subtitle = "Filter out events spanning longer than 24 hours",
                            checked = settings.hideMultiDay,
                            onCheckedChange = { checked ->
                                val updated = settings.copy(hideMultiDay = checked)
                                coroutineScope.launch {
                                    settingsRepo.updateSettings(updated)
                                    CleanCalendarWidget().updateAll(applicationContext)
                                }
                            }
                        )

                        // Remove on end time
                        SettingToggle(
                            title = "Remove Past Events Immediately",
                            subtitle = "Hide events right after their scheduled end time",
                            checked = settings.removeOnEnd,
                            onCheckedChange = { checked ->
                                val updated = settings.copy(removeOnEnd = checked)
                                coroutineScope.launch {
                                    settingsRepo.updateSettings(updated)
                                    CleanCalendarWidget().updateAll(applicationContext)
                                }
                            }
                        )

                        // Calendar colors
                        SettingToggle(
                            title = "Use Calendar Colors",
                            subtitle = "Color event titles using individual calendar category colors",
                            checked = settings.useCalendarColors,
                            onCheckedChange = { checked ->
                                val updated = settings.copy(useCalendarColors = checked)
                                coroutineScope.launch {
                                    settingsRepo.updateSettings(updated)
                                    CleanCalendarWidget().updateAll(applicationContext)
                                }
                            }
                        )

                        HorizontalDivider()

                        // Add to Homescreen Button
                        val appWidgetManager = AppWidgetManager.getInstance(this@MainActivity)
                        if (appWidgetManager.isRequestPinAppWidgetSupported) {
                            Button(
                                onClick = {
                                    val myProvider = ComponentName(this@MainActivity, CleanCalendarWidgetReceiver::class.java)
                                    appWidgetManager.requestPinAppWidget(myProvider, null, null)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Add Widget to Homescreen")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingNumberInput(
    title: String,
    subtitle: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.width(84.dp)
        )
    }
}

@Composable
fun SettingToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
