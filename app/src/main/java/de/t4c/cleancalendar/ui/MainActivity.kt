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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        if (!hasPermission) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Calendar permission is required for CleanCalendar widget.",
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(onClick = {
                                        permissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                                    }) {
                                        Text("Grant Calendar Permission")
                                    }
                                }
                            }
                        }

                        // Section 1: Appearance & Typography
                        SettingsSection(title = "Appearance") {
                            // Text Color Palette Picker
                            val textColorPresets = listOf(
                                "White" to 0xFFFFFFFFL,
                                "Pink / Rose" to 0xFFFF69B4L,
                                "Pastel Pink" to 0xFFFFB6C1L,
                                "Black" to 0xFF000000L,
                                "Dark Gray" to 0xFF212121L,
                                "Salbei Green" to 0xFFA7BEADL,
                                "Cyan / Mint" to 0xFF80CBC4L,
                                "Sunny Gold" to 0xFFFFD54FL,
                                "Lilac / Lavender" to 0xFFCE93D8L
                            )
                            SettingColorPicker(
                                title = "Text Color",
                                subtitle = "Primary font color for events and dates",
                                selectedColorHex = settings.textColorHex,
                                presets = textColorPresets,
                                onColorSelected = { newHex ->
                                    val updated = settings.copy(textColorHex = newHex)
                                    coroutineScope.launch {
                                        settingsRepo.updateSettings(updated)
                                        CleanCalendarWidget().updateAll(applicationContext)
                                    }
                                }
                            )

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            // Background Color Palette Picker
                            val bgColorPresets = listOf(
                                "Transparent" to 0x00000000L,
                                "Translucent Black (30%)" to 0x4D000000L,
                                "Translucent Black (60%)" to 0x99000000L,
                                "Solid Dark" to 0xFF1E1E1EL,
                                "Translucent White (30%)" to 0x4DFFFFFFL,
                                "Translucent White (60%)" to 0x99FFFFFFL,
                                "Solid White" to 0xFFFFFFFFL,
                                "Soft Salbei" to 0x80A7BEADL,
                                "Dark Charcoal" to 0xCC121212L
                            )
                            SettingColorPicker(
                                title = "Widget Background",
                                subtitle = "Container background color and transparency",
                                selectedColorHex = settings.backgroundColorHex,
                                presets = bgColorPresets,
                                onColorSelected = { newHex ->
                                    val updated = settings.copy(backgroundColorHex = newHex)
                                    coroutineScope.launch {
                                        settingsRepo.updateSettings(updated)
                                        CleanCalendarWidget().updateAll(applicationContext)
                                    }
                                }
                            )

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            var maxItemsText by remember(settings.maxItems) { mutableStateOf(settings.maxItems.toString()) }
                            SettingNumberInput(
                                title = "Visible Events",
                                subtitle = "Maximum number of events shown (1 - 50)",
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

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            var fontSizeText by remember(settings.fontSizeSp) { mutableStateOf(settings.fontSizeSp.toString()) }
                            SettingNumberInput(
                                title = "Font Size",
                                subtitle = "Text size in sp (8 - 36)",
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
                        }

                        // Section 2: Display & Grouping
                        SettingsSection(title = "Layout & Formatting") {
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

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

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

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

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
                        }

                        // Section 3: Event Filters
                        SettingsSection(title = "Event Filters") {
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

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

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

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

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
                        }

                        // Add to Homescreen Button
                        val appWidgetManager = AppWidgetManager.getInstance(this@MainActivity)
                        if (appWidgetManager.isRequestPinAppWidgetSupported) {
                            Button(
                                onClick = {
                                    val myProvider = ComponentName(this@MainActivity, CleanCalendarWidgetReceiver::class.java)
                                    appWidgetManager.requestPinAppWidget(myProvider, null, null)
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Add Widget to Homescreen", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                content()
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.width(68.dp).height(44.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingColorPicker(
    title: String,
    subtitle: String,
    selectedColorHex: Long,
    presets: List<Pair<String, Long>>,
    onColorSelected: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(10.dp))

        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(presets.size) { i ->
                val (name, colorLong) = presets[i]
                val isSelected = selectedColorHex == colorLong
                val color = androidx.compose.ui.graphics.Color(colorLong)
                val isTransparent = (colorLong and 0xFF000000L) == 0L

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onColorSelected(colorLong) }
                ) {
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(42.dp)
                            .then(
                                if (isSelected) {
                                    Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                } else {
                                    Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                }
                            )
                            .padding(3.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (isTransparent) androidx.compose.ui.graphics.Color.Transparent else color,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (isTransparent) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text("Ø", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = name.split(" ").first(),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
