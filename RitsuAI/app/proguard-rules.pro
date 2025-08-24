# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Ritsu AI classes
-keep class com.ritsuai.** { *; }

# Keep accessibility service
-keep class com.ritsuai.services.RitsuAccessibilityService { *; }

# Keep overlay service
-keep class com.ritsuai.services.RitsuOverlayService { *; }

# Keep data classes
-keep class com.ritsuai.*Response { *; }
-keep class com.ritsuai.*Data { *; }
-keep class com.ritsuai.*Config { *; }

# Keep enums
-keepclassmembers enum com.ritsuai.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Retrofit and Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class retrofit2.** { *; }

# Keep Room database
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Keep text-to-speech
-keep class android.speech.tts.** { *; }

# Keep accessibility classes
-keep class android.accessibilityservice.** { *; }
-keep class android.view.accessibility.** { *; }

