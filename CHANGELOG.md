# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.2] - 2026-10-28

### Fixed
- **Permission Card Branding:** Corrected app reference in permission prompt card to "SmoothCalendar NG".
- **Localization:** Renamed German color swatch label "Salbei" to "Sage" in the English interface.
- **Palette Swatch Labels:** Resolved duplicate "Black" label issue by preserving full descriptive names ("Dark 30%", "Dark 60%", "Light 30%", "Solid Dark").
- **Input Validation:** Added visual error indicators and validation feedback for the visible events count setting when input exceeds the allowed 1–50 range.
- **Widget Placement:** Added user guidance toast for successful pin requests and an informative fallback dialog with manual placement instructions for launchers that restrict or do not support automatic widget pinning (e.g., POCO/MIUI).
- **Background Clarity:** Added explicit explanation of the transparent background indicator (Ø) in the settings subtitle.

## [1.0.1] - 2026-10-20

### Changed
- Refined settings layout and typography controls.
- Updated Play Store compliance assets and documentation.

## [1.0.0] - 2026-10-18

### Added
- **Jetpack Glance Widget Core:**
  - Modern declarative Android AppWidget implementation based on `androidx.glance`.
  - Native 100% transparent container background option with customizable opacity slider.
  - Smart date aggregation grouping multiple events under a single date header.
  - Dynamic relative date indicators for "Today" and "Tomorrow".
  - Click actions directly linking to specific calendar events (`CalendarContract.Events`) or launching the default system calendar.
- **Calendar Data Engine:**
  - Fast projection-optimized queries on Android's `CalendarContract.Instances`.
  - Configurable filters: item count limits, lookahead day range, all-day event exclusion, and multi-day filter.
  - Automatic eviction of past events based on configured `removeOnEnd` policy.
- **Material 3 Configuration App:**
  - Preferences editor written in Jetpack Compose.
  - Asynchronous, type-safe settings persistence using AndroidX DataStore Preferences.
  - Instant widget refresh triggers upon configuration updates.
- **Documentation & Assets:**
  - High-resolution dark mode homescreen widget screenshots and configuration preview.
  - Technical architecture and build guide.
