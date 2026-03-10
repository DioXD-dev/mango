# Kotlin
-keep class kotlin.** { *; }
-keepclassmembers class ** { @kotlin.jvm.JvmStatic *; }

# Media3 / ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Retrofit + Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Coil
-keep class coil.** { *; }

# LRC API model
-keep class com.example.mangoplayer.ui.screen.LrclibResponse { *; }

# Compose
-keep class androidx.compose.** { *; }
