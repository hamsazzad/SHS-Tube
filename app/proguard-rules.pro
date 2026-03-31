# Add project specific ProGuard rules here.

# Keep yt-dlp Android library
-keep class com.yausername.** { *; }
-keep class com.yausername.youtubedl_android.** { *; }
-keep class com.yausername.ffmpeg.** { *; }

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Keep Compose
-keep class androidx.compose.** { *; }

# Keep OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Keep Coil
-keep class coil.** { *; }

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# Keep Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.shsshobuj.shstube.**$$serializer { *; }
-keepclassmembers class com.shsshobuj.shstube.** {
    *** Companion;
}
-keepclasseswithmembers class com.shsshobuj.shstube.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep SecretConfig
-keep class com.shsshobuj.shstube.config.SecretConfig { *; }

# General Android rules
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
