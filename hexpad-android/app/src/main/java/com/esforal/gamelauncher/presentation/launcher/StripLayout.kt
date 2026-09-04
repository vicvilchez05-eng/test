package com.esforal.gamelauncher.presentation.launcher

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
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
import com.esforal.gamelauncher.domain.model.PlayerStats
import com.esforal.gamelauncher.presentation.LauncherUiState
import com.esforal.gamelauncher.presentation.background.AnimatedBackground
import com.esforal.gamelauncher.presentation.common.DrawableImage
import com.esforal.gamelauncher.presentation.common.LocalAppImageSource
import com.esforal.gamelauncher.presentation.components.GlassPanel
import com.esforal.gamelauncher.presentation.components.PrimaryButton
import com.esforal.gamelauncher.presentation.components.ThemedIconButton
import com.esforal.gamelauncher.presentation.components.focusRing
import com.esforal.gamelauncher.presentation.nowplaying.NowPlayingChip
import com.esforal.gamelauncher.presentation.performance.PerformanceBar
import com.esforal.gamelauncher.presentation.theme.Theme
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Disposicion de franja, traida de la version de escritorio.
 *
 * Es la pantalla que en Hexpad PC se llama "Consola": los juegos mandan. Una
 * franja de iconos grandes ocupa la parte alta —los juegos primero y las
 * funciones al final, como iconos mas—, el elegido crece y su nombre aparece en
 * grande debajo, y de fondo su ilustracion. La zona baja es pequena a
 * proposito: una ficha compacta con "Jugar" y, al lado, a que se jugo hace poco.
 *
 * **Que cambia respecto al escritorio y por que.**
 *
 * Las estanterias de arriba son dos y no seis. El escritorio reparte la
 * biblioteca en Todos, Recientes, Favoritos y una por genero; en el movil no
 * hay favoritos ni generos —el genero salia de la ficha de internet, y este
 * launcher no pide red a proposito—, asi que quedan Todos y Recientes, y
 * Recientes solo aparece cuando el acceso al uso esta concedido y hay algo que
 * ensenar. Una pestana que siempre esta vacia es peor que no tenerla.
 *
 * Las funciones del final son tres y no cuatro: biblioteca, perfil y ajustes.
 * "Mi PC" y "Mantenimiento" son secciones del escritorio que aqui no existen;
 * lo que en el escritorio es "Mi PC" es en el movil la barra de rendimiento,
 * que va arriba junto al reloj. Se anade "+", que en el escritorio no hace
 * falta porque los juegos se detectan solos y aqui los anade el usuario.
 *
 * El bloque de "Tu PC" de la zona baja no se copia como panel propio por lo
 * mismo: seria repetir la barra de rendimiento con otras cifras.
 */
@Composable
internal fun StripLayout(
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

    var shelf by remember { mutableStateOf(Shelf.ALL) }
    val recent = recentlyPlayedPages(pages, state.playerStats)
    // Si Recientes deja de tener contenido -se revoco el acceso al uso, o el
    // usuario quito ese juego- la pestana desaparece y hay que volver a Todos,
    // o la franja se quedaria vacia sin explicacion.
    if (shelf == Shelf.RECENT && recent.isEmpty()) shelf = Shelf.ALL

    val visible = when (shelf) {
        Shelf.ALL -> pages.orEmpty()
        Shelf.RECENT -> recent
    }
    val selectedPage = visible.firstOrNull { it.packageName == selectedPackage }
    val index = visible.indexOfFirst { it.packageName == selectedPackage }

    val row = rememberLazyListState()

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground(
            theme = theme,
            animated = state.settings.animatedBackground && selectedPage?.wallpaperUri == null,
        )

        StripBackdrop(wallpaperUri = selectedPage?.wallpaperUri)

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 18.dp, vertical = 10.dp),
        ) {
            // Reparto explicito, y en este orden: primero lo que no cede -la
            // zona baja, que lleva "Jugar"-, y las portadas se quedan con lo
            // que sobre. Es la misma regla que en la disposicion de consola:
            // en una Column que desborda el que se queda sin alto es el ultimo
            // hijo, y ahi el ultimo es el boton de jugar.
            val cardHeight = (maxHeight * BOTTOM_FRACTION)
                .coerceIn(BOTTOM_HEIGHT_MIN, BOTTOM_HEIGHT_MAX)
            val tile = ((maxHeight - cardHeight - STRIP_CHROME_HEIGHT) / SELECTED_TILE_GROWTH)
                .coerceIn(TILE_SIZE_MIN, TILE_SIZE_MAX)
            val bigTile = tile * SELECTED_TILE_GROWTH

            // Con la pantalla muy baja, "Continuar jugando" es lo primero que
            // sobra: es un atajo, y la ficha con "Jugar" no lo es.
            val showRecentPanel = maxHeight >= RECENT_PANEL_MIN_HEIGHT

            LaunchedEffect(index, tile) {
                if (index >= 0) row.animateScrollToItem(index)
            }

            Column(modifier = Modifier.fillMaxSize()) {

                StripHeader(
                    shelf = shelf,
                    showRecentShelf = recent.isNotEmpty(),
                    onShelf = { shelf = it },
                    state = state,
                    telemetry = telemetry,
                    performanceExpanded = performanceExpanded,
                    actions = actions,
                    maxPanelHeight = maxHeight - STRIP_HEADER_HEIGHT,
                )

                Spacer(Modifier.height(10.dp))

                when {
                    pages == null -> Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator(color = theme.primary) }

                    pages.isEmpty() -> Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center,
                    ) { EmptyState(onAddGame = actions.onAddGame) }

                    else -> {
                        // La franja: los juegos y, al final, las funciones.
                        LazyRow(
                            state = row,
                            horizontalArrangement = Arrangement.spacedBy(TILE_GAP),
                            verticalAlignment = Alignment.Top,
                            // El centro lo decide la seleccion, igual que en el
                            // escritorio. Lo que alli era la rueda del raton
                            // aqui es el arrastre horizontal.
                            userScrollEnabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(bigTile + TILE_LABEL_SPACE)
                                .selectionDrag(
                                    pages = visible,
                                    index = index,
                                    onSelect = actions.onSelectPage,
                                ),
                        ) {
                            items(items = visible, key = { it.packageName }) { page ->
                                GameStripTile(
                                    page = page,
                                    selected = page.packageName == selectedPackage,
                                    size = tile,
                                    bigSize = bigTile,
                                    onSelect = { actions.onSelectPage(page) },
                                )
                            }

                            item(key = STRIP_SEPARATOR_KEY) {
                                Spacer(Modifier.width(FUNCTION_GAP))
                            }
                            item(key = STRIP_ADD_KEY) {
                                FunctionTile(
                                    icon = Icons.Filled.Add,
                                    label = stringResource(R.string.cd_add_game),
                                    size = tile,
                                    onClick = actions.onAddGame,
                                )
                            }
                            item(key = STRIP_LIBRARY_KEY) {
                                FunctionTile(
                                    icon = Icons.Filled.GridView,
                                    label = stringResource(R.string.cd_library),
                                    size = tile,
                                    onClick = actions.onOpenLibrary,
                                )
                            }
                            item(key = STRIP_PROFILE_KEY) {
                                FunctionTile(
                                    icon = Icons.Filled.Person,
                                    label = stringResource(R.string.cd_profile),
                                    size = tile,
                                    onClick = actions.onOpenProfile,
                                )
                            }
                            item(key = STRIP_SETTINGS_KEY) {
                                FunctionTile(
                                    icon = Icons.Filled.Settings,
                                    label = stringResource(R.string.cd_settings),
                                    size = tile,
                                    onClick = actions.onOpenSettings,
                                )
                            }
                        }

                        if (selectedPage != null) {
                            Spacer(Modifier.height(10.dp))
                            // El nombre del elegido, como titulo de la pantalla.
                            Text(
                                text = selectedPage.displayName,
                                color = theme.textPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        Spacer(Modifier.weight(1f))

                        NowPlayingChip(
                            nowPlaying = nowPlaying.takeUnless { performanceExpanded },
                            onTogglePlayPause = actions.onTogglePlayPause,
                            onSkipToNext = actions.onSkipToNext,
                            showControls = false,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(bottom = 8.dp),
                        )

                        if (selectedPage != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth().height(cardHeight),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                CompactCard(
                                    page = selectedPage,
                                    onPlay = { actions.onPlay(selectedPage) },
                                    onOpenActions = { actions.onOpenActions(selectedPage) },
                                    modifier = Modifier
                                        .weight(CARD_WEIGHT)
                                        .fillMaxHeight(),
                                )
                                if (showRecentPanel) {
                                    RecentPanel(
                                        recent = recent,
                                        onSelect = actions.onSelectPage,
                                        modifier = Modifier
                                            .weight(RECENT_WEIGHT)
                                            .fillMaxHeight(),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---- Estanterias --------------------------------------------------------------

/**
 * Las dos estanterias que el movil puede sostener con datos reales.
 *
 * En el escritorio esto vive en el dominio porque alli es una regla con peso
 * —que cuenta como reciente y que generos merecen estanteria—. Aqui son dos
 * casos y la regla entera es "los que Android dice que se han jugado", asi que
 * se queda en la pantalla en vez de inflar el dominio con un enum de dos.
 */
private enum class Shelf { ALL, RECENT }

/**
 * Las paginas jugadas hace poco, en el orden que da Android y solo con las que
 * siguen anadidas al launcher.
 *
 * Devuelve vacio cuando falta el acceso al uso, que es lo que hace desaparecer
 * la pestana. Ensenar "Recientes" vacio cuando en realidad no se puede saber
 * seria decirle al usuario que no ha jugado, que es falso.
 */
private fun recentlyPlayedPages(
    pages: List<GamePage>?,
    stats: PlayerStats?,
): List<GamePage> {
    if (pages == null || stats == null || !stats.available) return emptyList()
    val byPackage = pages.associateBy { it.packageName }
    return stats.recentlyPlayed.mapNotNull { byPackage[it.packageName] }
}

// ---- Fondo --------------------------------------------------------------------

/**
 * La ilustracion del elegido detras de un velo.
 *
 * Aqui el fondo si lleva velo y en la disposicion de pagina no, por la misma
 * razon que en la de consola: con las portadas al frente, una captura a plena
 * intensidad por detras compite con ellas y deja de distinguirse cual es la
 * ficha activa.
 */
@Composable
private fun StripBackdrop(wallpaperUri: String?) {
    val theme = Theme.current
    var failed by remember(wallpaperUri) { mutableStateOf(false) }

    if (wallpaperUri == null || failed) return

    Box(Modifier.fillMaxSize()) {
        AsyncImage(
            model = wallpaperUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            onState = { if (it is AsyncImagePainter.State.Error) failed = true },
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to theme.backgroundStops.first().copy(alpha = 0.72f),
                    0.5f to theme.backgroundStops.first().copy(alpha = 0.82f),
                    1f to theme.backgroundStops.first().copy(alpha = 0.95f),
                )
            )
        )
    }
}

// ---- Cabecera -----------------------------------------------------------------

@Composable
private fun StripHeader(
    shelf: Shelf,
    showRecentShelf: Boolean,
    onShelf: (Shelf) -> Unit,
    state: LauncherUiState,
    telemetry: DeviceSnapshot,
    performanceExpanded: Boolean,
    actions: LauncherActions,
    maxPanelHeight: Dp,
) {
    val theme = Theme.current
    var now by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalTime.now()
            delay(STRIP_CLOCK_REFRESH_MILLIS)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().height(STRIP_HEADER_HEIGHT),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShelfTab(
            text = stringResource(R.string.shelf_all),
            active = shelf == Shelf.ALL,
            onClick = { onShelf(Shelf.ALL) },
        )
        if (showRecentShelf) {
            Spacer(Modifier.width(14.dp))
            ShelfTab(
                text = stringResource(R.string.shelf_recent),
                active = shelf == Shelf.RECENT,
                onClick = { onShelf(Shelf.RECENT) },
            )
        }

        Spacer(Modifier.weight(1f))

        // Lo que en el escritorio es la seccion "Mi PC" es aqui la barra de
        // rendimiento, y su sitio es arriba: abajo se disputaria el hueco con
        // la ficha, que es donde esta el unico boton que se pulsa de verdad.
        if (state.settings.showPerformanceBar) {
            PerformanceBar(
                snapshot = telemetry,
                hardware = state.hardware,
                expanded = performanceExpanded,
                onToggle = actions.onTogglePerformance,
                maxExpandedHeight = maxPanelHeight,
            )
            Spacer(Modifier.width(12.dp))
        }

        Text(
            text = now.format(DateTimeFormatter.ofPattern("HH:mm")),
            color = theme.textSecondary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp,
        )
    }
}

@Composable
private fun ShelfTab(text: String, active: Boolean, onClick: () -> Unit) {
    val theme = Theme.current
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(8.dp)
    val color by animateColorAsState(
        targetValue = if (active) theme.textPrimary else theme.textDim,
        animationSpec = tween(150),
        label = "shelfColor",
    )

    Text(
        text = text,
        color = color,
        fontSize = if (active) 14.sp else 12.sp,
        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
        modifier = Modifier
            .clip(shape)
            .focusRing(interactionSource, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
    )
}

// ---- Franja de iconos ----------------------------------------------------------

/**
 * Icono cuadrado de un juego. El elegido crece y se enciende con el acento; los
 * demas se apagan un poco.
 *
 * La imagen es el fondo que eligio el usuario y no el icono ampliado, por lo
 * mismo que en el resto del launcher: un icono de 48 dp estirado a 130 se ve
 * blando. Sin fondo propio, degradado del color del icono con el icono encima.
 */
@Composable
private fun GameStripTile(
    page: GamePage,
    selected: Boolean,
    size: Dp,
    bigSize: Dp,
    onSelect: () -> Unit,
) {
    val theme = Theme.current
    val images = LocalAppImageSource.current
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(theme.cornerRadius)

    val side by animateDpAsState(
        targetValue = if (selected) bigSize else size,
        animationSpec = tween(TILE_RESIZE_MILLIS),
        label = "tileSide",
    )
    val dim by animateFloatAsState(
        targetValue = if (selected) 1f else UNSELECTED_ALPHA,
        animationSpec = tween(TILE_RESIZE_MILLIS),
        label = "tileAlpha",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) theme.primary else theme.surfaceBorder,
        animationSpec = tween(TILE_RESIZE_MILLIS),
        label = "tileBorder",
    )

    var wallpaperFailed by remember(page.packageName, page.wallpaperUri) {
        mutableStateOf(false)
    }
    val wallpaperUri = page.wallpaperUri

    Box(
        modifier = Modifier
            .size(side)
            .alpha(dim)
            .clip(shape)
            .background(theme.surface.copy(alpha = theme.surfaceAlpha))
            .border(if (selected) 2.dp else theme.borderWidth, borderColor, shape)
            .focusRing(interactionSource, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onSelect,
            ),
        contentAlignment = Alignment.Center,
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
                    cacheKey = page.packageName + ":strip",
                    drawable = images.icon(page.packageName),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(side * STRIP_ICON_FRACTION),
                    maxSizePx = STRIP_ICON_MAX_PX,
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
    }
}

/** Icono cuadrado de una funcion, con su nombre debajo del icono. */
@Composable
private fun FunctionTile(
    icon: ImageVector,
    label: String,
    size: Dp,
    onClick: () -> Unit,
) {
    val theme = Theme.current
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(theme.cornerRadius)

    Column(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(theme.surface.copy(alpha = theme.surfaceAlpha))
            .border(theme.borderWidth, theme.surfaceBorder, shape)
            .focusRing(interactionSource, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = theme.textSecondary,
            modifier = Modifier.size(size * FUNCTION_ICON_FRACTION),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            color = theme.textSecondary,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ---- Zona baja ------------------------------------------------------------------

/** Portada pequena, nombre, paquete y "Jugar", en una sola fila. */
@Composable
private fun CompactCard(
    page: GamePage,
    onPlay: () -> Unit,
    onOpenActions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = Theme.current
    val images = LocalAppImageSource.current
    var wallpaperFailed by remember(page.packageName, page.wallpaperUri) {
        mutableStateOf(false)
    }
    val wallpaperUri = page.wallpaperUri

    // Sin velo debajo y con texto denso encima: si se queda con la opacidad del
    // tema, el nombre del juego se transparenta a traves de las cifras.
    GlassPanel(modifier = modifier, backgroundAlpha = DENSE_PANEL_ALPHA) {
        Row(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.surfaceElevated),
                contentAlignment = Alignment.Center,
            ) {
                if (wallpaperUri == null || wallpaperFailed) {
                    DrawableImage(
                        cacheKey = page.packageName + ":compact",
                        drawable = images.icon(page.packageName),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        maxSizePx = STRIP_ICON_MAX_PX,
                    )
                } else {
                    AsyncImage(
                        model = wallpaperUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        onState = {
                            if (it is AsyncImagePainter.State.Error) wallpaperFailed = true
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = page.displayName.uppercase(),
                    color = theme.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = page.packageName,
                    color = theme.textDim,
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.6.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!page.isInstalled) {
                    Spacer(Modifier.height(2.dp))
                    // El aviso de "no instalado" no cede nunca: sin el, "Jugar"
                    // aparece apagado sin explicar por que.
                    Text(
                        text = stringResource(R.string.carousel_not_installed),
                        color = theme.warn,
                        fontSize = 9.5.sp,
                        maxLines = 1,
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            PrimaryButton(
                text = stringResource(R.string.action_play),
                icon = Icons.Filled.PlayArrow,
                onClick = onPlay,
                enabled = page.isInstalled,
            )
            Spacer(Modifier.width(8.dp))
            ThemedIconButton(
                icon = Icons.Filled.MoreHoriz,
                contentDescription = stringResource(R.string.action_options),
                onClick = onOpenActions,
            )
        }
    }
}

/** Lo ultimo jugado, en miniaturas. Pulsar una la elige. */
@Composable
private fun RecentPanel(
    recent: List<GamePage>,
    onSelect: (GamePage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = Theme.current
    val images = LocalAppImageSource.current

    GlassPanel(modifier = modifier, backgroundAlpha = DENSE_PANEL_ALPHA) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.shelf_recent).uppercase(),
                color = theme.textDim,
                fontSize = 8.5.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.2.sp,
            )
            Spacer(Modifier.height(6.dp))

            if (recent.isEmpty()) {
                Text(
                    text = stringResource(R.string.strip_nothing_recent),
                    color = theme.textDim,
                    fontSize = 10.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    recent.take(RECENT_THUMBNAILS).forEach { page ->
                        val interactionSource = remember(page.packageName) {
                            MutableInteractionSource()
                        }
                        val shape = RoundedCornerShape(6.dp)
                        Box(
                            modifier = Modifier
                                .size(RECENT_THUMBNAIL_SIZE)
                                .clip(shape)
                                .background(theme.surfaceElevated)
                                .border(theme.borderWidth, theme.surfaceBorder, shape)
                                .focusRing(interactionSource, shape)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    role = Role.Button,
                                    onClick = { onSelect(page) },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            DrawableImage(
                                cacheKey = page.packageName + ":recent",
                                drawable = images.icon(page.packageName),
                                contentDescription = page.displayName,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize().padding(6.dp),
                                maxSizePx = STRIP_ICON_MAX_PX,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---- Medidas -------------------------------------------------------------------

/** Cuanto crece el icono elegido, el mismo valor que en el escritorio. */
private const val SELECTED_TILE_GROWTH = 1.28f

/** Lo que se apagan los iconos no elegidos. */
private const val UNSELECTED_ALPHA = 0.78f

private const val TILE_RESIZE_MILLIS = 180

/**
 * Lo que ocupa la pantalla por fuera de la franja y de la zona baja: cabecera,
 * separaciones y el nombre del juego elegido.
 */
private val STRIP_CHROME_HEIGHT = 92.dp

/**
 * Por debajo de este lado un icono deja de ser una portada y pasa a ser un
 * sello de color. Antes de bajar de aqui es preferible que la franja se recorte.
 */
private val TILE_SIZE_MIN = 64.dp

/** Tope del icono: mas grande no cabe con la zona baja en un movil. */
private val TILE_SIZE_MAX = 132.dp

private val TILE_GAP = 8.dp

/** Aire bajo la franja para que el icono elegido no toque el nombre. */
private val TILE_LABEL_SPACE = 6.dp

/** Separacion entre el ultimo juego y la primera funcion. */
private val FUNCTION_GAP = 12.dp

private const val FUNCTION_ICON_FRACTION = 0.32f

/** Parte del alto que se lleva la zona baja, y sus topes. */
private const val BOTTOM_FRACTION = 0.26f
private val BOTTOM_HEIGHT_MIN = 84.dp
private val BOTTOM_HEIGHT_MAX = 118.dp

/** Reparto de la zona baja entre la ficha y las miniaturas. */
private const val CARD_WEIGHT = 2f
private const val RECENT_WEIGHT = 1f

private val RECENT_THUMBNAIL_SIZE = 38.dp
private const val RECENT_THUMBNAILS = 4

/** Alto util por debajo del cual "Continuar jugando" deja sitio a la ficha. */
private val RECENT_PANEL_MIN_HEIGHT = 320.dp

private val STRIP_HEADER_HEIGHT = 34.dp

private const val STRIP_ICON_FRACTION = 0.42f
private const val STRIP_ICON_MAX_PX = 160

/**
 * Opacidad de los paneles de la zona baja. La del tema deja intuir el fondo
 * animado, pero estos llevan texto denso y no tienen velo debajo.
 */
private const val DENSE_PANEL_ALPHA = 0.95f

private const val STRIP_CLOCK_REFRESH_MILLIS = 30_000L

private const val STRIP_SEPARATOR_KEY = "strip-separator"
private const val STRIP_ADD_KEY = "strip-add"
private const val STRIP_LIBRARY_KEY = "strip-library"
private const val STRIP_PROFILE_KEY = "strip-profile"
private const val STRIP_SETTINGS_KEY = "strip-settings"
