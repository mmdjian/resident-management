# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

-keep class android.support.v7.app.ActionBar { *; }
-keep class android.support.v7.widget.Toolbar { *; }
-keep class androidx.appcompat.app.ActionBar { *; }
-keep class androidx.appcompat.widget.Toolbar { *; }

# Keep POI classes
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**
