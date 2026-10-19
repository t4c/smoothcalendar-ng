# Proguard rules for SmoothCalendar NG
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep Glance and AppWidget receivers & internal Glance remote views machinery
-keep class androidx.glance.** { *; }
-keep interface androidx.glance.** { *; }
-keep class androidx.glance.appwidget.** { *; }
-keep interface androidx.glance.appwidget.** { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }

# Keep App code, models, and UI
-keep class de.t4c.cleancalendar.** { *; }
-keepclassmembers class de.t4c.cleancalendar.** { *; }

# Keep RemoteViews & View layouts from resource shrinking
-keep class android.widget.RemoteViews { *; }

# Keep DataStore & Coroutines internals
-dontwarn androidx.datastore.**
-dontwarn kotlinx.coroutines.**
