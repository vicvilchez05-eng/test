package com.defensezone.game

import android.app.Activity
import android.webkit.JavascriptInterface

/** Funciones nativas expuestas al juego como `window.Android`. */
class AndroidBridge(private val activity: Activity) {

    /** Botón "Salir" del menú principal. */
    @JavascriptInterface
    fun exitApp() {
        activity.runOnUiThread { activity.finishAndRemoveTask() }
    }

    @JavascriptInterface
    fun isAndroid(): Boolean = true
}
