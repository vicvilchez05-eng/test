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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.esforal.gamelauncher.domain.model.PlayerStats
import com.esforal.gamelauncher.presentation.LauncherUiState
import com.esforal.gamelauncher.presentation.background.AnimatedBackground
import com.esforal.gamelauncher.presentation.common.DrawableImage
import com.esforal.gamelauncher.presentation.common.formatLastPlayed
import com.esforal.gamelauncher.presentation.common.formatPlayTime
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
            // El alto es el recurso escaso y las cartas son lo unico que puede
            // ceder: la cabecera lleva los mandos y la fila de abajo lleva
            // "Jugar". Se reparte explicitamente, que es la leccion de §3.bis
            // del handoff —lo que absorbe todo el ajuste es lo que desaparece—.
            val cardHeight = (maxHeight - CAROUSEL_FIXED_HEIGHT)
                .coerceIn(CARD_HEIGHT_MIN, CARD_HEIGHT_MAX)
            val cardWidth = cardHeight * COVER_RATIO
            val smallHeight = cardHeight * UNSELECTED_SCALE
            val smallWidth = smallHeight * COVER_RATIO

            // La carta elegida queda centrada: el relleno lateral es lo que
            // sobra a cada lado de una carta grande, y la fila se desplaza a
            // su inicio. Es el mismo truco que en el escritorio.
            val sidePadding = ((maxWidth - cardWidth) / 2).coerceAtLeast(0.dp)

            // Por debajo de este alto los datos de la partida sobran antes que
            // el nombre: son cuatro cifras que el usuario puede mirar en el
            // perfil, y el nombre es lo que dice que juego esta elegido.
            val showData = maxHeight >= DATA_ROW_MIN_HEIGHT

            // Dentro de la Column el receptor de BoxWithConstraints queda
            // tapado por el de Column, asi que el alto libre se captura aqui.
            val panelMaxHeight = maxHeight - HEADER_HEIGHT

            LaunchedEffect(index, cardWidth) {
                if (index >= 0) row.animateScrollToItem(index, 0)
            }

            Column(modifier = Modifier.fillMaxSize()) {

                CarouselHeader(
                    state = state,
                    telemetry = telemetry,
                    performanceExpanded = performanceExpanded,
                    showLibrary = !pages.isNullOrEmpty(),
                    actions = actions,
                    maxPanelHeight = panelMaxHeight,
                )

                Spacer(Modifier.weight(1f))

                when {
                    pages == null -> Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator(color = theme.primary) }

                    pages.isEmpty() -> Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) { EmptyState(onAddGame = actions.onAddGame) }

                    else -> {
                        LazyRow(
                            state = row,
                            contentPadding = PaddingValues(horizontal = sidePadding),
                            horizontalArrangement = Arrangement.spacedBy(CARD_GAP),
                            verticalAlignment = Alignment.CenterVertically,
                            // Igual que en el escritorio: el centro lo decide la
                            // seleccion, no el dedo. Lo que alli era la rueda del
                            // raton aqui es el arrastre horizontal, que mueve la
                            // seleccion de una en una unas lineas mas abajo.
                            userScrollEnabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(cardHeight + CARD_HALO_SPACE)
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
                                    onPlay = { actions.onPlay(page) },
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Sonando ahora en su propia banda, como en la disposicion de
                // consola: si no suena nada la banda no existe, y no hay
                // ninguna constante de alto que mantener sincronizada.
                NowPlayingChip(
                    nowPlaying = nowPlaying.takeUnless { performanceExpanded },
                    onTogglePlayPause = actions.onTogglePlayPause,
                    onSkipToNext = actions.onSkipToNext,
                    showControls = false,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                if (selectedPage != null) {
                    SelectedGameData(
                        page = selectedPage,
                        playerStats = state.playerStats,
                        showData = showData,
                        onPlay = { actions.onPlay(selectedPage) },
                        onOpenActions = { actions.onOpenActions(selectedPage) },
                    )
                }

                Spacer(Modifier.weight(1f))
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

        // Velos: arriba para la cabecera, abajo para el nombre y las cifras. En
        // medio se aclara, que es donde estan las cartas y donde el fondo tiene
        // que dejarse ver.
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to theme.backgroundStops.first().copy(alpha = 0.62f),
                    0.5f to theme.backgroundStops.first().copy(alpha = 0.38f),
                    1f to theme.backgroundStops.first().copy(alpha = 0.92f),
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
        modifier = Modifier.fillMaxWidth().height(HEADER_HEIGHT),
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
    onPlay: () -> Unit,
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

        // Velo inferior para el texto, mas alto en la elegida porque lleva mas.
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    (if (selected) 0.45f else 0.6f) to Color.Transparent,
                    1f to Color.Black.copy(alpha = 0.88f),
                )
            )
        )

        if (!selected) {
            Box(
                Modifier.fillMaxSize().background(
                    theme.backgroundStops.first().copy(alpha = 0.45f)
                )
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = if (selected) {
                Alignment.CenterHorizontally
            } else {
                Alignment.Start
            },
        ) {
            Text(
                text = page.displayName.uppercase(),
                color = Color.White,
                fontSize = if (selected) 15.sp else 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = if (selected) TextAlign.Center else TextAlign.Start,
            )
            Text(
                text = page.packageName,
                color = Color.White.copy(alpha = 0.78f),
                fontSize = if (selected) 10.5.sp else 9.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (selected) {
                Spacer(Modifier.height(10.dp))
                PrimaryButton(
                    text = stringResource(R.string.action_play),
                    icon = Icons.Filled.PlayArrow,
                    onClick = onPlay,
                    enabled = page.isInstalled,
                )
            }
        }
    }
}

// ---- Datos del elegido --------------------------------------------------------

/**
 * Nombre del juego y sus datos de partida, bajo el carrusel.
 *
 * El nombre comparte fila con los botones, que es la correccion que ya se hizo
 * en la disposicion de consola: encima costaba alto que salia de las cartas, y
 * al lado no cuesta ninguno porque en horizontal lo que sobra es ancho. Lleva
 * `weight(fill = false)` para que coja lo que necesita y ni un dp mas; con
 * ancho fijo empujaria los botones fuera de pantalla en un movil estrecho.
 *
 * Los datos son los que Android publica de verdad. El escritorio ensena aqui la
 * tienda de procedencia y el veredicto frente a los requisitos del juego, y
 * ninguna de las dos cosas existe en el movil: no hay tiendas —el juego es una
 * app instalada— ni fichas con requisitos que comparar. En su lugar van la
 * ultima vez, el tiempo jugado y si sigue instalado.
 */
@Composable
private fun SelectedGameData(
    page: GamePage,
    playerStats: PlayerStats?,
    showData: Boolean,
    onPlay: () -> Unit,
    onOpenActions: () -> Unit,
) {
    val theme = Theme.current
    val playtime = playerStats
        ?.takeIf { it.available }
        ?.recentlyPlayed
        ?.firstOrNull { it.packageName == page.packageName }

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

        if (showData) {
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Los dos formateadores son los del propio launcher: el de la
                // ultima vez lo resuelve DateUtils, que ya viene traducido con
                // el sistema, y el del tiempo jugado usa las mismas cadenas que
                // el perfil. Escribir aqui otros dos daria dos formatos
                // distintos para el mismo dato en dos pantallas.
                DataPoint(
                    label = stringResource(R.string.carousel_last_played),
                    value = playtime
                        ?.let { formatLastPlayed(it.lastPlayedEpochMillis) }
                        ?: stringResource(R.string.carousel_no_usage_data),
                )
                DataPoint(
                    label = stringResource(R.string.carousel_played),
                    value = playtime
                        ?.let { formatPlayTime(it.playTimeMillis) }
                        ?: stringResource(R.string.carousel_no_usage_data),
                )
                DataPoint(
                    label = stringResource(R.string.carousel_state),
                    value = if (page.isInstalled) {
                        stringResource(R.string.carousel_installed)
                    } else {
                        stringResource(R.string.carousel_not_installed)
                    },
                    color = if (page.isInstalled) null else theme.warn,
                )
            }
        }
    }
}

@Composable
private fun DataPoint(label: String, value: String, color: Color? = null) {
    val theme = Theme.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label.uppercase(),
            color = theme.textDim,
            fontSize = 8.5.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp,
            maxLines = 1,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            color = color ?: theme.textSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ---- Medidas -----------------------------------------------------------------

/** Proporcion de portada vertical, 2:3, la misma que en el escritorio. */
private const val COVER_RATIO = 2f / 3f

/** Lo que mide una carta no elegida respecto a la elegida. */
private const val UNSELECTED_SCALE = 0.74f

private const val CARD_RESIZE_MILLIS = 220

/**
 * Lo que ocupa la pantalla por fuera del carrusel y no puede encogerse: la
 * cabecera, la fila del nombre con sus botones, la de los datos y las
 * separaciones. Lo que quede es de las cartas.
 */
private val CAROUSEL_FIXED_HEIGHT = 148.dp

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

/**
 * Alto util por debajo del cual los datos de la partida se retiran. Se miran
 * de vez en cuando y estan tambien en el perfil; el nombre y "Jugar", no.
 */
private val DATA_ROW_MIN_HEIGHT = 340.dp

private val HEADER_HEIGHT = 44.dp
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
