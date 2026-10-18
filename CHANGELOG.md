# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
