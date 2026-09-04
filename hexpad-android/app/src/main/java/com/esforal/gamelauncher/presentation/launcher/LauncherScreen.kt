package com.esforal.gamelauncher.presentation.launcher

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.esforal.gamelauncher.R
import com.esforal.gamelauncher.domain.model.DeviceSnapshot
import com.esforal.gamelauncher.domain.model.GamePage
import com.esforal.gamelauncher.domain.model.HomeLayout
import com.esforal.gamelauncher.domain.model.NowPlaying
import com.esforal.gamelauncher.presentation.LauncherUiState
import com.esforal.gamelauncher.presentation.background.AnimatedBackground
import com.esforal.gamelauncher.presentation.components.ThemedIconButton
import com.esforal.gamelauncher.presentation.nowplaying.NowPlayingChip
import com.esforal.gamelauncher.presentation.performance.PerformanceBar
import com.esforal.gamelauncher.presentation.theme.Theme

/**
 * Acciones del dashboard, agrupadas para que la firma de la pantalla se lea de
 * un vistazo y anadir una no obligue a tocar cada llamada intermedia.
 */
data class LauncherActions(
    val onOpenSettings: () -> Unit,
    val onOpenProfile: () -> Unit,
    val onTogglePerformance: () -> Unit,
    val onAddGame: () -> Unit,
    val onSelectPage: (GamePage) -> Unit,
    val onMovePage: (packageName: String, toIndex: Int) -> Unit,
    val onPlay: (GamePage) -> Unit,
    val onOpenActions: (GamePage) -> Unit,
    val onTogglePlayPause: () -> Unit,
    val onSkipToNext: () -> Unit,
    val onOpenLibrary: () -> Unit,
)

/**
 * Pantalla principal.
 *
 * Reparte a una de las cuatro disposiciones. No comparten un arbol comun a
 * proposito: lo que cambia entre ellas es donde va cada cosa, y forzar un solo
 * arbol con condiciones en cada modificador habria salido mas enredado que dos
 * funciones que se leen de arriba abajo. Lo que si se comparte son los
 * componentes -la fila de paginas, la barra de rendimiento, los botones-, que
 * es donde de verdad duele duplicar.
 */
@Composable
fun LauncherScreen(
    state: LauncherUiState,
    telemetry: DeviceSnapshot,
    nowPlaying: NowPlaying?,
    selectedPackage: String?,
    performanceExpanded: Boolean,
    actions: LauncherActions,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    when (state.settings.homeLayout) {
        HomeLayout.PAGE -> PageLayout(
            state = state,
            telemetry = telemetry,
            nowPlaying = nowPlaying,
            selectedPackage = selectedPackage,
            performanceExpanded = performanceExpanded,
            actions = actions,
            contentPadding = contentPadding,
            modifier = modifier,
        )

        HomeLayout.CONSOLE -> ConsoleLayout(
            state = state,
            telemetry = telemetry,
            nowPlaying = nowPlaying,
            selectedPackage = selectedPackage,
            performanceExpanded = performanceExpanded,
            actions = actions,
            contentPadding = contentPadding,
            modifier = modifier,
        )

        HomeLayout.CAROUSEL -> CarouselLayout(
            state = state,
            telemetry = telemetry,
            nowPlaying = nowPlaying,
            selectedPackage = selectedPackage,
            performanceExpanded = performanceExpanded,
            actions = actions,
            contentPadding = contentPadding,
            modifier = modifier,
        )

        HomeLayout.STRIP -> StripLayout(
            state = state,
            telemetry = telemetry,
            nowPlaying = nowPlaying,
            selectedPackage = selectedPackage,
            performanceExpanded = performanceExpanded,
            actions = actions,
            contentPadding = contentPadding,
            modifier = modifier,
        )
    }
}

/**
 * Dashboard horizontal.
 *
 * Tres capas superpuestas: fondo animado del tema, pagina del juego a sangre y
 * mandos flotando encima. Superponer en vez de apilar en columnas es lo que
 * permite que el fondo del juego ocupe toda la pantalla —que es lo que pide el
 * diseno— sin que los mandos se coman alto, que en horizontal es lo escaso.
 */
@Composable
private fun PageLayout(
    state: LauncherUiState,
    telemetry: DeviceSnapshot,
    nowPlaying: NowPlaying?,
    selectedPackage: String?,
    performanceExpanded: Boolean,
    actions: LauncherActions,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val theme = Theme.current
    val layoutDirection = LocalLayoutDirection.current
    val pages = state.pages
    val selectedPage = pages?.firstOrNull { it.packageName == selectedPackage }

    Box(modifier = modifier.fillMaxSize()) {
        // El fondo animado solo se anima donde algo lo deja pasar. La pagina de
        // un juego con fondo propio es una imagen opaca a sangre, asi que lo
        // tapa entero: seguir animando detras seria gastar GPU en algo que nadie
        // ve, y justo antes de lanzar un juego, que es cuando menos sobra. Con
        // `animated` a false no se dibuja quieto, es que no se suscribe al reloj
        // de frames y no consume nada.
        //
        // Las paginas sin fondo propio caen a un degradado translucido, asi que
        // ahi si se ve y sigue animando. Salvedad conocida: si el fondo elegido
        // ya no existe, la pagina cae a ese mismo degradado pero aqui todavia
        // consta que tiene fondo, asi que se queda quieto. Se acepta: es un
        // estado degradado y el fondo del tema se sigue pintando igual.
        val backgroundIsVisible = selectedPage?.wallpaperUri == null

        AnimatedBackground(
            theme = theme,
            animated = state.settings.animatedBackground && backgroundIsVisible,
        )

        // La pagina del juego ocupa toda la pantalla, por detras de los
        // mandos: el fondo tiene que llegar a los cuatro bordes o deja de
        // parecer un entorno y pasa a parecer una tarjeta. Es la propia pagina
        // la que aparta su texto de la barra superior y de las pestanas.
        if (selectedPage != null) {
            AnimatedContent(
                targetState = selectedPage,
                transitionSpec = {
                    (slideInHorizontally(tween(260)) { it / 8 } + fadeIn(tween(260)))
                        .togetherWith(fadeOut(tween(180)))
                },
                label = "gamePage",
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                GamePageContent(
                    page = page,
                    onPlay = { actions.onPlay(page) },
                    onOpenMenu = { actions.onOpenActions(page) },
                    // Los mandos se dibujan DENTRO de los insets del
                    // sistema, asi que el hueco que hay que reservarles es el
                    // inset mas su propio alto. Sin sumar el inset, el icono
                    // del juego se metia debajo del boton de ajustes en cuanto
                    // el nombre ocupaba dos lineas.
                    chromePadding = PaddingValues(
                        start = contentPadding.calculateStartPadding(layoutDirection),
                        end = contentPadding.calculateEndPadding(layoutDirection),
                        top = contentPadding.calculateTopPadding() + CHROME_TOP_SPACE,
                        bottom = contentPadding.calculateBottomPadding() + CHROME_BOTTOM_SPACE,
                    ),
                )
            }
        }

        // Estados sin pagina: se centran en el area libre de mandos.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(top = CHROME_TOP_SPACE, bottom = CHROME_BOTTOM_SPACE),
        ) {
            when {
                pages == null -> CircularProgressIndicator(
                    color = theme.primary,
                    modifier = Modifier.align(Alignment.Center),
                )

                pages.isEmpty() -> EmptyState(
                    onAddGame = actions.onAddGame,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }

        // Capa de mandos.
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            // El panel de rendimiento crece hacia abajo y no sabe nada de las
            // pestanas, asi que sin este tope se les echa encima. Se le deja
            // todo el alto libre menos el que ocupan las pestanas.
            val performanceMaxHeight = maxHeight - CHROME_BOTTOM_SPACE

            ThemedIconButton(
                icon = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.cd_settings),
                onClick = actions.onOpenSettings,
                modifier = Modifier.align(Alignment.TopStart),
            )

            ThemedIconButton(
                icon = Icons.Filled.Person,
                contentDescription = stringResource(R.string.cd_profile),
                onClick = actions.onOpenProfile,
                modifier = Modifier.align(Alignment.TopEnd),
            )

            if (state.settings.showPerformanceBar) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PerformanceBar(
                        snapshot = telemetry,
                        hardware = state.hardware,
                        expanded = performanceExpanded,
                        onToggle = actions.onTogglePerformance,
                        maxExpandedHeight = performanceMaxHeight,
                    )
                }
            }

            // Sonando ahora: abajo a la derecha, justo encima de las
            // pestanas. Es el unico hueco que no se disputa con nada -la
            // pagina del juego ocupa el 55 % izquierdo, los mandos estan
            // arriba y las pestanas crecen desde la izquierda-.
            //
            // Se retira con el panel de rendimiento desplegado: ese panel se
            // centra y baja hasta las pestanas, asi que se le echaria encima.
            // Y quien lo despliega esta mirando cifras, no la musica.
            NowPlayingChip(
                nowPlaying = nowPlaying.takeUnless { performanceExpanded },
                onTogglePlayPause = actions.onTogglePlayPause,
                onSkipToNext = actions.onSkipToNext,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = CHROME_BOTTOM_SPACE),
            )

            if (!pages.isNullOrEmpty()) {
                // La rejilla se abre desde el extremo de la barra de paginas y
                // no desde la fila de arriba: es navegacion entre juegos, y su
                // sitio esta con las pestanas. Arriba, ademas, habria que
                // apartar la barra de rendimiento para hacerle hueco.
                //
                // Va fija fuera de la LazyRow y no como ultimo elemento de la
                // lista: dentro se desplazaria con las pestanas, y el boton que
                // sirve para no perderse seria justo el que desaparece en
                // cuanto hay muchos juegos.
                GameTabs(
                    pages = pages,
                    selectedPackage = selectedPackage,
                    onSelect = actions.onSelectPage,
                    onAddGame = actions.onAddGame,
                    onMove = actions.onMovePage,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(end = LIBRARY_BUTTON_SPACE),
                )

                ThemedIconButton(
                    icon = Icons.Filled.GridView,
                    contentDescription = stringResource(R.string.cd_library),
                    onClick = actions.onOpenLibrary,
                    modifier = Modifier.align(Alignment.BottomEnd),
                )
            }
        }
    }
}

/** Ancho del boton de la rejilla mas su separacion de la ultima pestana. */
private val LIBRARY_BUTTON_SPACE = 58.dp

internal val CHROME_TOP_SPACE = 74.dp
internal val CHROME_BOTTOM_SPACE = 82.dp
