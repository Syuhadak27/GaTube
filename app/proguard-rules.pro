# Aturan ProGuard/R8 untuk GaTube.

# NewPipeExtractor memakai refleksi dan parser JSON/JS, jadi jangan di-obfuscate.
-keep class org.schabi.newpipe.extractor.** { *; }
-keep class org.mozilla.javascript.** { *; }
-keep class com.google.protobuf.** { *; }

# Model Gson.
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep class com.nontontv.indonesia.data.remote.dto.** { *; }

# Media3.
-keep class androidx.media3.** { *; }

# Umum.
-dontwarn org.mozilla.javascript.**
-dontwarn com.google.protobuf.**
