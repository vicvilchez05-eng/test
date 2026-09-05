# ESFshoot — reglas de ProGuard/R8
-keepclassmembers class * extends android.webkit.WebView { public *; }
-keep class androidx.webkit.** { *; }
