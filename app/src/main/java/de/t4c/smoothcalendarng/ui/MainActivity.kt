package de.t4c.smoothcalendarng.ui

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import de.t4c.smoothcalendarng.BuildConfig
import de.t4c.smoothcalendarng.widget.CleanCalendarWidgetReceiver
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
import androidx.compose.foundation.layout.size
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
import de.t4c.smoothcalendarng.data.SettingsRepository
import de.t4c.smoothcalendarng.model.WidgetSettings
import de.t4c.smoothcalendarng.widget.CleanCalendarWidget
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
                            title = { Text("SmoothCalendar NG") },
                            actions = {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            CleanCalendarWidget().updateAll(applicationContext)
                                            android.widget.Toast.makeText(
                                                applicationContext,
                                                "Widget refreshed & settings applied!",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text("Reload")
                                }
                            },
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
                            // Text Color Palette Picker (Top 5 presets + Custom)
                            val textColorPresets = listOf(
                                "White" to 0xFFFFFFFFL,
                                "Black" to 0xFF000000L,
                                "Dark Gray" to 0xFF333333L,
                                "Salbei" to 0xFFA7BEADL,
                                "Gold" to 0xFFFFD54FL
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

                            // Background Color Palette Picker (Top 5 presets + Custom)
                            val bgColorPresets = listOf(
                                "Transparent" to 0x00000000L,
                                "Black (30%)" to 0x4D000000L,
                                "Black (60%)" to 0x99000000L,
                                "White (30%)" to 0x4DFFFFFFL,
                                "Solid Dark" to 0xFF1E1E1EL
                            )
                            SettingColorPicker(
                                title = "Widget Background",
                                subtitle = "Container background color and transparency",
                                selectedColorHex = settings.backgroundColorHex,
                                presets = bgColorPresets,
                                allowAlpha = true,
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
                                    val filtered = input.filter { it.isDigit() }.take(2)
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

                            // Quick Font Size Buttons (8, 10, 11, 12, 14, 16)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(8, 10, 11, 12, 14, 16).forEach { size ->
                                    val isSelected = settings.fontSizeSp == size
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .clickable {
                                                fontSizeText = size.toString()
                                                val updated = settings.copy(fontSizeSp = size)
                                                coroutineScope.launch {
                                                    settingsRepo.updateSettings(updated)
                                                    CleanCalendarWidget().updateAll(applicationContext)
                                                }
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                            Text(
                                                text = "${size}sp",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
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

                        // Minimal About & Support Footer
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val context = LocalContext.current
                            Text(
                                text = "SmoothCalendar NG v${BuildConfig.VERSION_NAME}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "GitHub",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/t4c/smoothcalendar-ng"))
                                        context.startActivity(intent)
                                    }
                                )
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                                Text(
                                    text = "Buy me a coffee ☕",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/t4c2"))
                                        context.startActivity(intent)
                                    }
                                )
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
    allowAlpha: Boolean = false,
    onColorSelected: (Long) -> Unit
) {
    var showCustomDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            // Custom Color Button / Preview
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = androidx.compose.ui.graphics.Color(selectedColorHex),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .width(44.dp)
                    .height(44.dp)
                    .clickable { showCustomDialog = true }
            ) {
                if ((selectedColorHex and 0xFF000000L) == 0L) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("Ø", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            presets.forEach { (name, colorLong) ->
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

            // Exactly 6th item: The '+' Custom Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { showCustomDialog = true }
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(42.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Custom",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showCustomDialog) {
        FullColorPickerDialog(
            initialColorHex = selectedColorHex,
            allowAlpha = allowAlpha,
            onDismiss = { showCustomDialog = false },
            onColorConfirmed = { chosenHex ->
                onColorSelected(chosenHex)
                showCustomDialog = false
            }
        )
    }
}

@Composable
fun FullColorPickerDialog(
    initialColorHex: Long,
    allowAlpha: Boolean,
    onDismiss: () -> Unit,
    onColorConfirmed: (Long) -> Unit
) {
    var a by remember { mutableStateOf(((initialColorHex shr 24) and 0xFF).toFloat() / 255f) }
    var r by remember { mutableStateOf(((initialColorHex shr 16) and 0xFF).toFloat() / 255f) }
    var g by remember { mutableStateOf(((initialColorHex shr 8) and 0xFF).toFloat() / 255f) }
    var b by remember { mutableStateOf((initialColorHex and 0xFF).toFloat() / 255f) }

    val currentHexLong = remember(a, r, g, b, allowAlpha) {
        val alphaInt = if (allowAlpha) (a * 255).toInt() and 0xFF else 0xFF
        val rInt = (r * 255).toInt() and 0xFF
        val gInt = (g * 255).toInt() and 0xFF
        val bInt = (b * 255).toInt() and 0xFF
        ((alphaInt.toLong() shl 24) or (rInt.toLong() shl 16) or (gInt.toLong() shl 8) or bInt.toLong()) and 0xFFFFFFFFL
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Color Palette", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                // Large Preview & Hex display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = androidx.compose.ui.graphics.Color(currentHexLong),
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
                    ) {}

                    Column {
                        Text(
                            text = String.format("#%08X", currentHexLong),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "RGB: ${(r * 255).toInt()}, ${(g * 255).toInt()}, ${(b * 255).toInt()}" +
                                    if (allowAlpha) " (A: ${(a * 100).toInt()}%)" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Quick spectrum chips
                val spectrum = listOf(
                    0xFFF44336L, 0xFFE91E63L, 0xFF9C27B0L, 0xFF673AB7L, 0xFF3F51B5L,
                    0xFF2196F3L, 0xFF03A9F4L, 0xFF00BCD4L, 0xFF009688L, 0xFF4CAF50L,
                    0xFF8BC34AL, 0xFFCDDC39L, 0xFFFFEB3BL, 0xFFFFC107L, 0xFFFF9800L,
                    0xFFFF5722L, 0xFF795548L, 0xFF9E9E9EL, 0xFF607D8BL, 0xFFFFFFFFL, 0xFF000000L
                )

                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(spectrum.size) { idx ->
                        val hex = spectrum[idx]
                        Surface(
                            modifier = Modifier
                                .size(28.dp)
                                .clickable {
                                    r = ((hex shr 16) and 0xFF).toFloat() / 255f
                                    g = ((hex shr 8) and 0xFF).toFloat() / 255f
                                    b = (hex and 0xFF).toFloat() / 255f
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = androidx.compose.ui.graphics.Color(hex),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {}
                    }
                }

                // Sliders
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Red Slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("R", fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.Red, modifier = Modifier.width(24.dp))
                        androidx.compose.material3.Slider(
                            value = r,
                            onValueChange = { r = it },
                            modifier = Modifier.weight(1f)
                        )
                        Text("${(r * 255).toInt()}", modifier = Modifier.width(36.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall)
                    }

                    // Green Slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("G", fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.Green, modifier = Modifier.width(24.dp))
                        androidx.compose.material3.Slider(
                            value = g,
                            onValueChange = { g = it },
                            modifier = Modifier.weight(1f)
                        )
                        Text("${(g * 255).toInt()}", modifier = Modifier.width(36.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall)
                    }

                    // Blue Slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("B", fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.Blue, modifier = Modifier.width(24.dp))
                        androidx.compose.material3.Slider(
                            value = b,
                            onValueChange = { b = it },
                            modifier = Modifier.weight(1f)
                        )
                        Text("${(b * 255).toInt()}", modifier = Modifier.width(36.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall)
                    }

                    // Alpha Slider (if permitted)
                    if (allowAlpha) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("A", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(24.dp))
                            androidx.compose.material3.Slider(
                                value = a,
                                onValueChange = { a = it },
                                modifier = Modifier.weight(1f)
                            )
                            Text("${(a * 100).toInt()}%", modifier = Modifier.width(36.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onColorConfirmed(currentHexLong) }) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}
