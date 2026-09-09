# Conserva los métodos expuestos a JavaScript a través de addJavascriptInterface.
-keepclassmembers class com.defensezone.game.AndroidBridge {
    @android.webkit.JavascriptInterface <methods>;
}
