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

-keep class com.shockwave.**
-keep class com.batuscode.pdfium.PathData {
    public *;
}


# Firebase AppCheck ve Play Integrity için
-keep class com.google.firebase.appcheck.** { *; }
-keep class com.google.firebase.appcheck.playintegrity.** { *; }
-keep class com.google.firebase.appcheck.debug.** { *; }
-keep class com.google.firebase.appcheck.safetynet.** { *; }

# Play Integrity API için (Google Play Services)
-keep class com.google.android.gms.tasks.** { *; }
-keep class com.google.android.play.core.integrity.** { *; }

# Firebase'in reflection kullandığı sınıflar
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Google Play Services (Play Integrity için)
-keep class com.google.android.play.core.** { *; }
-keep class com.google.android.gms.common.** { *; }
-keep class com.google.android.gms.tasks.** { *; }