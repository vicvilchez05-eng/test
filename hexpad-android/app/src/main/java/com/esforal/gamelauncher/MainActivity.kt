package com.esforal.gamelauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.esforal.gamelauncher.presentation.LauncherApp
import com.esforal.gamelauncher.presentation.LauncherViewModel

/**
 * Unica Activity de la app.
 *
 * Su trabajo se acaba aqui: coger el grafo de dependencias y montar Compose. El
 * tema no se instala en este punto sino dentro de [LauncherApp], porque depende
 * de una preferencia persistida que hay que leer antes.
 *
 * La orientacion horizontal se fija en el manifiesto, no aqui.
 *
 * Lo que si se fija aqui es la pantalla completa: ver [ocultarBarrasDelSistema].
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        ocultarBarrasDelSistema()

        val container = (application as GameLauncherApplication).container

        setContent {
            LauncherApp(
                viewModel = viewModel(factory = LauncherViewModel.factory(container)),
                appImageSource = container.appImageSource,
            )
        }
    }

    /**
     * Las barras del sistema se vuelven a ocultar cada vez que la ventana
     * recupera el foco.
     *
     * No es redundante con la llamada de [onCreate]: al volver de un juego, de
     * la pantalla de ajustes del sistema o del selector de imagen, la ventana
     * recupera el foco con las barras puestas otra vez. Sin esto, el launcher
     * solo se veria a pantalla completa en el primer arranque.
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) ocultarBarrasDelSistema()
    }

    /**
     * Pantalla completa de verdad: se ocultan la barra de estado y la de
     * navegacion, no solo se pintan transparentes.
     *
     * `enableEdgeToEdge` ya dejaba el fondo pasar por debajo de las barras,
     * pero la hora, las notificaciones y la barra de gestos seguian encima del
     * launcher. En horizontal el alto es el recurso escaso -unos 411 dp, y 309
     * con el tamano de pantalla subido-, asi que esas dos barras no son solo
     * ruido visual: son alto que le falta a las portadas.
     *
     * El comportamiento es el transitorio: deslizar desde un borde las trae de
     * vuelta un momento y se van solas. Es el unico modo que no deja al usuario
     * sin reloj ni notificaciones cuando las necesita, y ademas las barras
     * transitorias se dibujan encima sin empujar el contenido, asi que el
     * reparto del alto no se recalcula al asomarlas.
     */
    private fun ocultarBarrasDelSistema() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}
