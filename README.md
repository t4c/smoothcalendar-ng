# CleanCalendar

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)
[![Glance](https://img.shields.io/badge/Jetpack%20Glance-1.1.1-green.svg)](https://developer.android.com/jetpack/androidx/releases/glance)
[![MinSdk](https://img.shields.io/badge/MinSdk-26-orange.svg)](https://developer.android.com)

A modern, ultra-minimalist, transparent Android calendar widget built with **Jetpack Glance** (Compose for AppWidgets). Inspired by the legendary **Smooth Calendar**, engineered for simplicity, zero battery drain, and seamless home screen integration.

---

## Preview

| Transparent Widget on Dark Wallpaper | In-App Configuration |
| :---: | :---: |
| <img src="docs/screenshots/widget_transparent_dark.png" width="360" alt="CleanCalendar Widget Preview"/> | <img src="docs/screenshots/settings_screen.png" width="360" alt="CleanCalendar Settings Preview"/> |

---

## Features

* **True 100% Transparency:** Blends completely into your desktop wallpaper. Background opacity adjustable from 0% (fully transparent) to 100% solid.
* **Date Grouping:** Events occurring on the same day group cleanly under a single date label without visual clutter.
* **Relative Labels:** Highlights events for "Today" and "Tomorrow" dynamically.
* **Instant Action Routing:**
  * Tapping any specific event opens that exact appointment in your default Android calendar app.
  * Tapping empty widget space opens the calendar at the current time.
* **Flexible Filtering:**
  * Configurable lookahead window (e.g. 14 days) and item count cap (default: 4 events).
  * Toggle options to hide all-day events or multi-day spanning events.
  * Auto-eviction: past events disappear automatically once their end time has passed (`removeOnEnd`).
  * Support for individual calendar exclusions.
* **Zero Telemetry & Minimal Permissions:**
  * Only requests read access to calendar instances (`android.permission.READ_CALENDAR`).
  * No network permissions (`INTERNET` permission is absent).
  * Zero ads, zero tracking, zero background battery waste.
* **Material 3 Configuration App:**
  * Fast, native Jetpack Compose configuration UI to tweak appearance, numeric event limits, font sizes, date formats, and filters on the fly.
  * Organic Material 3 grouped card hierarchy with native pill inputs matching system switches.
  * Instant preference synchronization powered by AndroidX DataStore.

---

## Architecture & Tech Stack

CleanCalendar is architected with modern Android engineering best practices:

* **Jetpack Glance (`androidx.glance:glance-appwidget:1.1.1`):** Declarative widget layout rendering translating Compose trees directly into RemoteViews.
* **Android Calendar Provider (`CalendarContract.Instances`):** Efficient instance querying with projection pruning to maximize read speeds and reduce memory footprint.
* **AndroidX DataStore (`Preferences`):** Reactive, type-safe persistence for widget preferences.
* **Jetpack Compose + Material 3:** Modern, accessible user interface for the in-app settings screen.

---

## Building from Source

### Prerequisites
* JDK 17 or higher
* Android SDK Platform 35 (`compileSdk = 35`, `minSdk = 26`)

---

## Installation & Updates

### Obtainium (Recommended)
CleanCalendar is built to work seamlessly with [Obtainium](https://github.com/ImranR98/Obtainium) for automatic background updates directly from GitHub Releases:
1. Open **Obtainium** on your Android device.
2. Tap **Add App** and paste: `https://github.com/t4c/cleancalendar`
3. Tap **Add** – Obtainium tracks new release tags and installs APK updates directly.

### Manual Download
Download the latest `CleanCalendar-v*.apk` directly from the [GitHub Releases](https://github.com/t4c/cleancalendar/releases) page.

---

### Build Commands

```bash
# Clone the repository
git clone https://github.com/t4c/cleancalendar.git
cd cleancalendar

# Build Debug APK
./gradlew assembleDebug

# Output APK path:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## License

CleanCalendar is Free Software licensed under the **GNU Affero General Public License v3.0 (AGPL-3.0-or-later)**. See the [LICENSE](LICENSE) file for the full license text.
