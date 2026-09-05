package com.esforal.gamelauncher.presentation.launcher

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.esforal.gamelauncher.R
import com.esforal.gamelauncher.domain.model.DeviceSnapshot
import com.esforal.gamelauncher.domain.model.GamePage
import com.esforal.gamelauncher.domain.model.NowPlaying
import com.esforal.gamelauncher.presentation.LauncherUiState
import com.esforal.gamelauncher.presentation.background.AnimatedBackground
import com.esforal.gamelauncher.presentation.common.DrawableImage
import com.esforal.gamelauncher.presentation.common.LocalAppImageSource
import com.esforal.gamelauncher.presentation.components.GhostButton
import com.esforal.gamelauncher.presentation.components.GlassPanel
import com.esforal.gamelauncher.presentation.components.PrimaryButton
import com.esforal.gamelauncher.presentation.components.ThemedIconButton
import com.esforal.gamelauncher.presentation.components.focusRing
import com.esforal.gamelauncher.presentation.nowplaying.NowPlayingChip
import com.esforal.gamelauncher.presentation.performance.PerformanceBar
import com.esforal.gamelauncher.presentation.theme.Theme
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Disposicion de carrusel, traida de la version de escritorio.
 *
 * Es la pantalla que en Hexpad PC se llama "Rejilla": una fila de cartas
 * verticales con la elegida en el centro, mas grande y encendida con el
 * acento; las demas la flanquean a menor tamano. De fondo, el fondo del juego
 * elegido, nitido y algo ampliado, con velos arriba y abajo para que el texto
 * se lea encima. Arriba la marca y la hora; debajo del carrusel, el nombre y
 * los datos de la partida.
 *
 * **Que cambia respecto al escritorio y por que.** Alli la navegacion vive en
 * una barra lateral, que aqui no existe: los mandos de la app —biblioteca,
 * perfil y ajustes— se meten en la cabecera, entre la marca y el reloj, porque
 * es la unica banda que ya estaba ocupada y no cuesta alto. Una barra inferior
 * propia habria costado 58 dp de los 411 que hay, y el alto es justo lo que
 * necesitan las cartas.
 *
 * El halo de la carta elegida no es un desenfoque como en el escritorio sino
 * los anillos concentricos de [GlassPanel]: `Modifier.blur` solo dibuja desde
 * API 31, asi que en un movil con Android 8 a 11 la carta elegida se habria
 * quedado sin halo y solo con el borde. Los anillos se ven iguales en todas
 * las versiones, y son ademas lo que ya usa el resto del launcher.
 */
@Composable
internal fun CarouselLayout(
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
    val pages = state.pages
    val selectedPage = pages?.firstOrNull { it.packageName == selectedPackage }
    val index = pages?.indexOfFirst { it.packageName == selectedPackage } ?: -1

    val row = rememberLazyListState()

    Box(modifier = modifier.fillMaxSize()) {
        // Mismo criterio que en las otras dos disposiciones: el fondo animado
        // no se suscribe al reloj de frames cuando hay una captura opaca
        // delante. Es GPU que el juego va a necesitar dentro de un segundo.
        AnimatedBackground(
            theme = theme,
            animated = state.settings.animatedBackground && selectedPage?.wallpaperUri == null,
        )

        CarouselBackdrop(
            wallpaperUri = selectedPage?.wallpaperUri,
            animated = state.settings.animatedBackground,
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 18.dp, vertical = 10.dp),
        ) {
            // Dentro de la Column el receptor de BoxWithConstraints queda
            // tapado por el de Column, asi que el alto libre se captura aqui.
            val panelMaxHeight = maxHeight - HEADER_HEIGHT

            Column(modifier = Modifier.fillMaxSize()) {

                CarouselHeader(
                    state = state,
                    telemetry = telemetry,
                    performanceExpanded = performanceExpanded,
                    showLibrary = !pages.isNullOrEmpty(),
                    actions = actions,
                    maxPanelHeight = panelMaxHeight,
                )

                // El carrusel se queda con lo que sobre y la carta se mide
                // contra ese hueco.
                //
                // Antes el alto de la carta salia de un presupuesto fijo y el
                // resto lo repartian dos espaciadores con peso. Eran dos
                // cuentas que no cuadraban: en cuanto la carta topaba con su
                // tope, o el presupuesto no acertaba, quedaba pantalla vacia
                // abajo y el contenido apelotonado arriba. Con weight hay una
                // sola cuenta y la hace Compose.
                //
                // De paso desaparece la reserva de la banda de "sonando ahora":
                // si aparece, sale de este hueco sola.
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    val cardHeight = (maxHeight - CARD_HALO_SPACE)
                        .coerceIn(CARD_HEIGHT_MIN, CARD_HEIGHT_MAX)
                    val cardWidth = cardHeight * COVER_RATIO
                    val smallHeight = cardHeight * UNSELECTED_SCALE
                    val smallWidth = smallHeight * COVER_RATIO

                    // La carta elegida queda centrada: el relleno lateral es lo
                    // que sobra a cada lado de una carta grande, y la fila se
                    // desplaza a su inicio. Es el mismo truco que en el
                    // escritorio.
                    val sidePadding = ((maxWidth - cardWidth) / 2).coerceAtLeast(0.dp)

                    LaunchedEffect(index, cardWidth) {
                        if (index >= 0) row.animateScrollToItem(index, 0)
                    }

                    when {
                        pages == null -> CircularProgressIndicator(color = theme.primary)

                        pages.isEmpty() -> EmptyState(onAddGame = actions.onAddGame)

                        else -> LazyRow(
                            state = row,
                            contentPadding = PaddingValues(horizontal = sidePadding),
                            horizontalArrangement = Arrangement.spacedBy(CARD_GAP),
                            verticalAlignment = Alignment.CenterVertically,
                            // Igual que en el escritorio: el centro lo decide la
                            // seleccion, no el dedo. Lo que alli era la rueda
                            // del raton aqui es el arrastre horizontal.
                            userScrollEnabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectionDrag(
                                    pages = pages,
                                    index = index,
                                    onSelect = actions.onSelectPage,
                                ),
                        ) {
                            items(items = pages, key = { it.packageName }) { page ->
                                CarouselCard(
                                    page = page,
                                    selected = page.packageName == selectedPackage,
                                    width = cardWidth,
                                    height = cardHeight,
                                    smallWidth = smallWidth,
                                    smallHeight = smallHeight,
                                    onSelect = { actions.onSelectPage(page) },
                                )
                            }
                        }
                    }
                }

                // Banda propia: si no suena nada, no existe.
                NowPlayingChip(
                    nowPlaying = nowPlaying.takeUnless { performanceExpanded },
                    onTogglePlayPause = actions.onTogglePlayPause,
                    onSkipToNext = actions.onSkipToNext,
                    showControls = false,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                if (selectedPage != null) {
                    Spacer(Modifier.height(10.dp))
                    SelectedGameData(
                        page = selectedPage,
                        onPlay = { actions.onPlay(selectedPage) },
                        onOpenActions = { actions.onOpenActions(selectedPage) },
                    )
                }
            }
        }
    }
}

// ---- Fondo -------------------------------------------------------------------

/**
 * El fondo del juego elegido, nitido y algo ampliado, con velos arriba y abajo.
 *
 * En el escritorio se probo desenfocado y el usuario lo quiso nitido: la
 * ilustracion es el juego. El zoom lentisimo —52 s de ida y otros tantos de
 * vuelta— es lo que da sensacion de entorno vivo sin que nada se mueva a la
 * vista; se para con el ajuste de fondo animado, igual que todo lo demas.
 */
@Composable
private fun CarouselBackdrop(wallpaperUri: String?, animated: Boolean) {
    val theme = Theme.current
    var failed by remember(wallpaperUri) { mutableStateOf(false) }

    val scale = if (animated) {
        val transition = rememberInfiniteTransition(label = "carouselZoom")
        val value by transition.animateFloat(
            initialValue = BACKDROP_ZOOM_MIN,
            targetValue = BACKDROP_ZOOM_MAX,
            animationSpec = infiniteRepeatable(
                animation = tween(BACKDROP_ZOOM_MILLIS, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "carouselZoomValue",
        )
        value
    } else {
        BACKDROP_ZOOM_STILL
    }

    Box(Modifier.fillMaxSize().clipToBounds()) {
        if (wallpaperUri != null && !failed) {
            AsyncImage(
                model = wallpaperUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onState = { if (it is AsyncImagePainter.State.Error) failed = true },
                modifier = Modifier.fillMaxSize().scale(scale),
            )
        }

        // Velo solo donde hay texto: arriba la cabecera, abajo el nombre y las
        // cifras. En medio, donde estan las cartas, se aclara casi del todo
        // para que el fondo del juego se vea de verdad y no solo se intuya.
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to theme.backgroundStops.first().copy(alpha = 0.50f),
                    0.28f to theme.backgroundStops.first().copy(alpha = 0.08f),
                    0.58f to theme.backgroundStops.first().copy(alpha = 0.08f),
                    1f to theme.backgroundStops.first().copy(alpha = 0.68f),
                )
            )
        )
    }
}

// ---- Cabecera ----------------------------------------------------------------

/**
 * Marca a la izquierda, hora y fecha a la derecha, y en medio lo que en el
 * escritorio vivia en la barra lateral: rendimiento y los tres mandos.
 */
@Composable
private fun CarouselHeader(
    state: LauncherUiState,
    telemetry: DeviceSnapshot,
    performanceExpanded: Boolean,
    showLibrary: Boolean,
    actions: LauncherActions,
    maxPanelHeight: Dp,
) {
    val theme = Theme.current
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(CLOCK_REFRESH_MILLIS)
        }
    }

    Row(
        // heightIn y no height: la barra de rendimiento pide 48 dp de
        // objetivo tactil, y con un alto fijo por debajo de eso sus cifras
        // se cortaban por arriba y por abajo.
        modifier = Modifier.fillMaxWidth().heightIn(min = HEADER_HEIGHT),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(BRAND_MARK_SIZE)
                .clip(RoundedCornerShape(theme.cornerRadius))
                .background(Brush.linearGradient(listOf(theme.primary, theme.secondary))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.SportsEsports,
                contentDescription = null,
                tint = theme.onPrimary,
                modifier = Modifier.size(BRAND_ICON_SIZE),
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = stringResource(R.string.app_name).uppercase(),
            color = theme.textPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            maxLines = 1,
        )

        Spacer(Modifier.weight(1f))

        if (state.settings.showPerformanceBar) {
            PerformanceBar(
                snapshot = telemetry,
                hardware = state.hardware,
                expanded = performanceExpanded,
                onToggle = actions.onTogglePerformance,
                maxExpandedHeight = maxPanelHeight,
            )
            Spacer(Modifier.width(10.dp))
        }

        if (showLibrary) {
            ThemedIconButton(
                icon = Icons.Filled.GridView,
                contentDescription = stringResource(R.string.cd_library),
                onClick = actions.onOpenLibrary,
            )
        }
        ThemedIconButton(
            icon = Icons.Filled.Person,
            contentDescription = stringResource(R.string.cd_profile),
            onClick = actions.onOpenProfile,
        )
        ThemedIconButton(
            icon = Icons.Filled.Settings,
            contentDescription = stringResource(R.string.cd_settings),
            onClick = actions.onOpenSettings,
        )

        Spacer(Modifier.width(10.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = now.format(DateTimeFormatter.ofPattern("HH:mm")),
                color = theme.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
            Text(
                // Patron localizado y no uno escrito a mano: el launcher esta
                // en dos idiomas, y "d 'de' MMMM" solo es correcto en uno.
                text = now.format(
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        .withLocale(Locale.getDefault())
                ).uppercase(),
                color = theme.textSecondary,
                fontSize = 9.5.sp,
                letterSpacing = 1.sp,
                maxLines = 1,
            )
        }
    }
}

// ---- Cartas ------------------------------------------------------------------

/**
 * Carta vertical con proporcion de portada, 2:3.
 *
 * La elegida crece, se enciende con el acento y ensena su nombre, su paquete y
 * "Jugar" dentro de la propia carta; las demas se encogen y se apagan un poco,
 * porque con varias portadas a todo color compitiendo la activa deja de
 * destacar aunque tenga borde.
 *
 * La portada es el fondo que eligio el usuario, no el icono ampliado: un icono
 * de Android es un recurso de 48 dp y estirado se ve blando. Sin fondo, o si el
 * fondo ya no existe, cae al degradado del color del icono con el icono encima,
 * que es el mismo desenlace que en las otras dos disposiciones.
 */
@Composable
private fun CarouselCard(
    page: GamePage,
    selected: Boolean,
    width: Dp,
    height: Dp,
    smallWidth: Dp,
    smallHeight: Dp,
    onSelect: () -> Unit,
) {
    val theme = Theme.current
    val images = LocalAppImageSource.current
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(theme.cornerRadius)

    val cardWidth by animateDpAsState(
        targetValue = if (selected) width else smallWidth,
        animationSpec = tween(CARD_RESIZE_MILLIS),
        label = "cardWidth",
    )
    val cardHeight by animateDpAsState(
        targetValue = if (selected) height else smallHeight,
        animationSpec = tween(CARD_RESIZE_MILLIS),
        label = "cardHeight",
    )

    var wallpaperFailed by remember(page.packageName, page.wallpaperUri) {
        mutableStateOf(false)
    }
    val wallpaperUri = page.wallpaperUri

    GlassPanel(
        glowing = selected,
        cornerRadius = theme.cornerRadius,
        // Opaca: la carta lleva una imagen a sangre, y un relleno translucido
        // por debajo no se ve pero cuesta una capa que componer.
        backgroundAlpha = 1f,
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
            .focusRing(interactionSource, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onSelect,
            ),
    ) {
        if (wallpaperUri == null || wallpaperFailed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(page.accentArgb).copy(alpha = 0.85f),
                                Color(page.accentArgb).copy(alpha = 0.3f),
                            )
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                DrawableImage(
                    cacheKey = page.packageName + ":carousel",
                    drawable = images.icon(page.packageName),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(cardWidth * ICON_FRACTION),
                    maxSizePx = ICON_MAX_PX,
                )
            }
        } else {
            AsyncImage(
                model = wallpaperUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onState = { if (it is AsyncImagePainter.State.Error) wallpaperFailed = true },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // La carta elegida no lleva nada encima: su nombre va debajo del
        // carrusel en grande y "Jugar" esta justo ahi al lado. Repetirlos aqui
        // tapaba la ilustracion en la unica carta que se ve entera, que es
        // precisamente lo que un carrusel viene a ensenar.
        if (!selected) {
            // Velo inferior para que el nombre se lea sobre la ilustracion, y
            // un apagado general encima: con varias portadas a todo color
            // compitiendo, la elegida deja de destacar aunque tenga borde.
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.6f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.88f),
                    )
                )
            )
            Box(
                Modifier.fillMaxSize().background(
                    theme.backgroundStops.first().copy(alpha = 0.45f)
                )
            )
            // Dos lineas: sin el nombre de paquete debajo sobra sitio, y un
            // titulo de dos palabras cortado en la primera no identifica nada.
            Text(
                text = page.displayName.uppercase(),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
            )
        }
    }
}

// ---- Datos del elegido --------------------------------------------------------

/**
 * Nombre del juego y lo unico que se pulsa de verdad aqui.
 *
 * El nombre comparte fila con los botones, que es la correccion que ya se hizo
 * en la disposicion de consola: encima costaba alto que salia de las cartas, y
 * al lado no cuesta ninguno porque en horizontal lo que sobra es ancho. Lleva
 * `weight(fill = false)` para que coja lo que necesita y ni un dp mas; con
 * ancho fijo empujaria los botones fuera de pantalla en un movil estrecho.
 *
 * Aqui iba tambien una fila con la ultima vez, el tiempo jugado y si el juego
 * sigue instalado. Se quito por lo mismo que la barra inferior de la franja:
 * son cifras que se miran de vez en cuando, estan enteras en el perfil, y el
 * alto que ocupaban vale mas como portada. Lo unico que sobrevive de aquello es
 * el aviso de "no instalado", que no es un dato sino la explicacion de por que
 * "Jugar" esta apagado.
 */
@Composable
private fun SelectedGameData(
    page: GamePage,
    onPlay: () -> Unit,
    onOpenActions: () -> Unit,
) {
    val theme = Theme.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = page.displayName.uppercase(),
                color = theme.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            PrimaryButton(
                text = stringResource(R.string.action_play),
                icon = Icons.Filled.PlayArrow,
                onClick = onPlay,
                enabled = page.isInstalled,
            )
            GhostButton(
                text = stringResource(R.string.action_options),
                icon = Icons.Filled.MoreHoriz,
                onClick = onOpenActions,
            )
        }

        if (!page.isInstalled) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.carousel_not_installed),
                color = theme.warn,
                fontSize = 11.sp,
                maxLines = 1,
            )
        }
    }
}

// ---- Medidas -----------------------------------------------------------------

/** Proporcion de portada vertical, 2:3, la misma que en el escritorio. */
private const val COVER_RATIO = 2f / 3f

/** Lo que mide una carta no elegida respecto a la elegida. */
private const val UNSELECTED_SCALE = 0.74f

private const val CARD_RESIZE_MILLIS = 220


/**
 * Por debajo de este lado la carta deja de leerse como una portada. Es
 * preferible que el carrusel se salga por los lados a que desaparezcan los
 * botones, que es lo unico que no cede.
 */
private val CARD_HEIGHT_MIN = 132.dp

/** Mas alla de esto la carta no crece: en una tablet quedaria desproporcionada. */
private val CARD_HEIGHT_MAX = 300.dp

/** Hueco entre cartas, y el aire que necesita el halo de la elegida. */
private val CARD_GAP = 16.dp
private val CARD_HALO_SPACE = 20.dp

private val HEADER_HEIGHT = 52.dp
private val BRAND_MARK_SIZE = 34.dp
private val BRAND_ICON_SIZE = 20.dp

/** Lado del icono dentro de una carta sin fondo propio, sobre el ancho de la carta. */
private const val ICON_FRACTION = 0.42f
private const val ICON_MAX_PX = 160

private const val BACKDROP_ZOOM_MIN = 1.10f
private const val BACKDROP_ZOOM_MAX = 1.22f
private const val BACKDROP_ZOOM_STILL = 1.14f
private const val BACKDROP_ZOOM_MILLIS = 52_000

private const val CLOCK_REFRESH_MILLIS = 20_000L
