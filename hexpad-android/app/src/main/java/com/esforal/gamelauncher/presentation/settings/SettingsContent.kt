package com.esforal.gamelauncher.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.esforal.gamelauncher.BuildConfig
import com.esforal.gamelauncher.R
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.esforal.gamelauncher.presentation.components.MINIMUM_TOUCH_TARGET
import com.esforal.gamelauncher.domain.model.HomeLayout
import com.esforal.gamelauncher.domain.model.LauncherSettings
import com.esforal.gamelauncher.domain.model.NowPlaying
import com.esforal.gamelauncher.domain.model.ThemeId
import com.esforal.gamelauncher.presentation.components.GhostButton
import com.esforal.gamelauncher.presentation.components.SectionLabel
import com.esforal.gamelauncher.presentation.theme.AllThemes
import com.esforal.gamelauncher.presentation.theme.LauncherTheme
import com.esforal.gamelauncher.presentation.theme.Theme

/**
 * Ajustes del launcher.
 *
 * Es un panel lateral y no una pantalla aparte: en horizontal sobra ancho, y
 * dejar ver el launcher por detras mantiene la sensacion de estar en el mismo
 * sitio en vez de haber navegado a otra aplicacion.
 */
@Composable
fun ColumnScope.SettingsContent(
    settings: LauncherSettings,
    hasUsageAccess: Boolean,
    hasMediaAccess: Boolean,
    mediaAccessMayBeBlocked: Boolean,
    /** Lo que el launcher esta leyendo ahora mismo, para poder confirmarlo. */
    nowPlaying: NowPlaying?,
    onSelectTheme: (ThemeId) -> Unit,
    onSelectHomeLayout: (HomeLayout) -> Unit,
    onToggleAnimatedBackground: (Boolean) -> Unit,
    onTogglePerformanceBar: (Boolean) -> Unit,
    onToggleThermalWarning: (Boolean) -> Unit,
    onToggleNowPlaying: (Boolean) -> Unit,
    onGrantUsageAccess: () -> Unit,
    onGrantMediaAccess: () -> Unit,
    onOpenLegal: () -> Unit,
) {
    val theme = Theme.current

    Text(
        text = stringResource(R.string.settings_title),
        style = MaterialTheme.typography.titleLarge,
        color = theme.textPrimary,
    )

    Column(
        modifier = Modifier
            .weight(1f, fill = false)
            .verticalScroll(rememberScrollState())
            .padding(top = 18.dp),
    ) {
        SectionLabel(stringResource(R.string.settings_section_theme))
        Spacer(Modifier.height(10.dp))

        AllThemes.forEach { (themeId, candidate) ->
            ThemeCard(
                theme = candidate,
                selected = themeId == settings.themeId,
                onClick = { onSelectTheme(themeId) },
            )
            Spacer(Modifier.height(9.dp))
        }

        Spacer(Modifier.height(14.dp))
        SectionLabel(stringResource(R.string.settings_section_layout))
        Spacer(Modifier.height(10.dp))

        // Tarjetas y no un interruptor: no es una opcion que se enciende, son
        // disposiciones con nombre propio, y hay que poder explicar cada una
        // antes de elegirla.
        //
        // Las dos ultimas vienen de Hexpad para Windows. Van al final y no
        // intercaladas porque el orden de esta lista es el de siempre: quien ya
        // conocia la app encuentra las suyas donde estaban.
        HomeLayoutCard(
            title = stringResource(R.string.settings_layout_page),
            subtitle = stringResource(R.string.settings_layout_page_hint),
            selected = settings.homeLayout == HomeLayout.PAGE,
            onClick = { onSelectHomeLayout(HomeLayout.PAGE) },
        )
        Spacer(Modifier.height(9.dp))
        HomeLayoutCard(
            title = stringResource(R.string.settings_layout_console),
            subtitle = stringResource(R.string.settings_layout_console_hint),
            selected = settings.homeLayout == HomeLayout.CONSOLE,
            onClick = { onSelectHomeLayout(HomeLayout.CONSOLE) },
        )
        Spacer(Modifier.height(9.dp))
        HomeLayoutCard(
            title = stringResource(R.string.settings_layout_carousel),
            subtitle = stringResource(R.string.settings_layout_carousel_hint),
            selected = settings.homeLayout == HomeLayout.CAROUSEL,
            onClick = { onSelectHomeLayout(HomeLayout.CAROUSEL) },
        )
        Spacer(Modifier.height(9.dp))
        HomeLayoutCard(
            title = stringResource(R.string.settings_layout_strip),
            subtitle = stringResource(R.string.settings_layout_strip_hint),
            selected = settings.homeLayout == HomeLayout.STRIP,
            onClick = { onSelectHomeLayout(HomeLayout.STRIP) },
        )

        Spacer(Modifier.height(14.dp))
        SectionLabel(stringResource(R.string.settings_section_interface))
        Spacer(Modifier.height(6.dp))

        SettingSwitch(
            title = stringResource(R.string.settings_animated_background),
            subtitle = stringResource(R.string.settings_animated_background_hint),
            checked = settings.animatedBackground,
            onCheckedChange = onToggleAnimatedBackground,
        )
        SettingSwitch(
            title = stringResource(R.string.settings_performance_bar),
            subtitle = stringResource(R.string.settings_performance_bar_hint),
            checked = settings.showPerformanceBar,
            onCheckedChange = onTogglePerformanceBar,
        )
        SettingSwitch(
            title = stringResource(R.string.settings_thermal_warning),
            subtitle = stringResource(R.string.settings_thermal_warning_hint),
            checked = settings.warnWhenThrottling,
            onCheckedChange = onToggleThermalWarning,
        )
        SettingSwitch(
            title = stringResource(R.string.settings_now_playing),
            subtitle = stringResource(R.string.settings_now_playing_hint),
            checked = settings.showNowPlaying,
            onCheckedChange = onToggleNowPlaying,
        )

        // La explicacion va aqui, pegada al interruptor y antes del boton que
        // lleva a Ajustes, y no en la seccion de permisos de mas abajo. No es
        // una preferencia de maquetacion: la politica de datos de usuario de
        // Play exige que la divulgacion aparezca en el momento en que se pide
        // el acceso, no en otro sitio de la app donde el usuario tendria que ir
        // a buscarla.
        //
        // Solo se ensena con la funcion encendida: a quien no la usa no le hace
        // falta leer un parrafo sobre un permiso que no se le va a pedir.
        if (settings.showNowPlaying) {
            Spacer(Modifier.height(4.dp))
            if (hasMediaAccess) {
                Text(
                    text = stringResource(R.string.settings_media_granted),
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.ok,
                )
                Spacer(Modifier.height(6.dp))
                // Con el permiso dado y nada sonando, la tarjeta no aparece, y
                // eso es indistinguible de que la funcion este rota. Aqui se
                // dice lo que el launcher esta leyendo de verdad, que es la
                // misma regla que sigue el resto de la app: cuando no hay dato,
                // decirlo en vez de no ensenar nada.
                Text(
                    text = if (nowPlaying == null) {
                        stringResource(R.string.settings_now_playing_idle)
                    } else {
                        stringResource(R.string.settings_now_playing_current, nowPlaying.title)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.textDim,
                )
            } else {
                Text(
                    text = stringResource(R.string.settings_now_playing_disclosure),
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.textDim,
                )
                Spacer(Modifier.height(10.dp))
                GhostButton(
                    text = stringResource(R.string.action_grant_media_access),
                    onClick = onGrantMediaAccess,
                )
                Spacer(Modifier.height(8.dp))
                // El boton lleva a una pantalla del sistema, y ahi hay
                // fabricantes que la esconden en otro sitio o la abren vacia.
                // Con la ruta escrita, quien se quede a medias puede terminar
                // el permiso a mano en vez de creer que la funcion no va.
                Text(
                    text = stringResource(R.string.settings_media_manual_path),
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.textDim,
                )
                // Instalada fuera de una tienda, Android 13 no deja activar el
                // interruptor por mucho que se insista, y no explica como
                // levantarlo. Sin este parrafo la funcion parece rota.
                if (mediaAccessMayBeBlocked) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.settings_media_restricted),
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.warn,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        Spacer(Modifier.height(14.dp))
        SectionLabel(stringResource(R.string.settings_section_permissions))
        Spacer(Modifier.height(8.dp))

        if (hasUsageAccess) {
            Text(
                text = stringResource(R.string.settings_usage_granted),
                style = MaterialTheme.typography.bodyMedium,
                color = theme.ok,
            )
        } else {
            Text(
                text = stringResource(R.string.settings_usage_missing),
                style = MaterialTheme.typography.bodyMedium,
                color = theme.textDim,
            )
            Spacer(Modifier.height(10.dp))
            GhostButton(
                text = stringResource(R.string.action_grant_usage_access),
                onClick = onGrantUsageAccess,
            )
        }

        Spacer(Modifier.height(18.dp))
        SectionLabel(stringResource(R.string.settings_section_about))
        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.about_body),
            style = MaterialTheme.typography.bodyMedium,
            color = theme.textDim,
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.about_founders),
            style = MaterialTheme.typography.bodyMedium,
            color = theme.textSecondary,
        )

        Spacer(Modifier.height(14.dp))

        // Abrir el aviso legal no contradice que la app no tenga permiso de
        // INTERNET: se lanza un intent y es el navegador —otra aplicacion— quien
        // hace la peticion. Hexpad sigue sin poder salir a la red por si misma.
        val legalLabel = stringResource(R.string.action_legal)
        val legalHint = stringResource(R.string.cd_opens_browser)

        GhostButton(
            text = legalLabel,
            icon = Icons.AutoMirrored.Filled.OpenInNew,
            onClick = onOpenLegal,
            // El icono de "abrir fuera" avisa a quien lo ve de que la accion
            // sale de la app; quien usa lector de pantalla no lo ve, y salir a
            // otra aplicacion sin esperarlo desorienta.
            modifier = Modifier.semantics { contentDescription = "$legalLabel. $legalHint" },
        )

        // La version, al final y sin ruido. No es decoracion: sin ella, dos APK
        // con el mismo nombre de fichero son indistinguibles una vez instalados,
        // y una captura no dice que build esta corriendo.
        Spacer(Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.app_name) + " " + BuildConfig.VERSION_NAME,
            style = MaterialTheme.typography.labelSmall,
            color = theme.textDim,
        )
    }
}

/**
 * Tarjeta de tema con muestra real de sus colores.
 *
 * La muestra se compone con los valores del propio tema, no con capturas: si
 * manana se retoca una paleta, la vista previa cambia sola.
 */
@Composable
private fun ThemeCard(theme: LauncherTheme, selected: Boolean, onClick: () -> Unit) {
    val active = Theme.current
    val shape = RoundedCornerShape(active.cornerRadius)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(active.surfaceElevated.copy(alpha = if (selected) 0.9f else 0.5f))
            .border(
                width = if (selected) 1.5.dp else active.borderWidth,
                color = if (selected) active.primary else active.surfaceBorder,
                shape = shape,
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .sizeIn(minHeight = MINIMUM_TOUCH_TARGET)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.verticalGradient(theme.backgroundStops)),
            contentAlignment = Alignment.Center,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(Modifier.size(9.dp).clip(CircleShape).background(theme.primary))
                Box(Modifier.size(9.dp).clip(CircleShape).background(theme.secondary))
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(
                text = stringResource(theme.displayNameRes),
                style = MaterialTheme.typography.titleSmall,
                color = active.textPrimary,
            )
            Text(
                text = stringResource(theme.taglineRes),
                style = MaterialTheme.typography.bodySmall,
                color = active.textDim,
            )
        }

        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = active.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * Tarjeta de disposicion. Misma forma que la del tema para que se lean como lo
 * que son: dos elecciones excluyentes, no ajustes sueltos.
 */
@Composable
private fun HomeLayoutCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val theme = Theme.current
    val shape = RoundedCornerShape(theme.cornerRadius)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(theme.surfaceElevated.copy(alpha = if (selected) 0.9f else 0.5f))
            .border(
                width = if (selected) 1.5.dp else theme.borderWidth,
                color = if (selected) theme.primary else theme.surfaceBorder,
                shape = shape,
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .sizeIn(minHeight = MINIMUM_TOUCH_TARGET)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = theme.textPrimary,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = theme.textDim,
            )
        }
        if (selected) {
            Spacer(Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = theme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val theme = Theme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // toggleable en la fila y el Switch sin accion propia: si ambos
            // fueran pulsables, el lector de pantalla anunciaria dos controles
            // para la misma opcion.
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
            .sizeIn(minHeight = MINIMUM_TOUCH_TARGET)
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = theme.textPrimary,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = theme.textDim,
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = theme.onPrimary,
                checkedTrackColor = theme.primary,
                uncheckedThumbColor = theme.textDim,
                uncheckedTrackColor = theme.surface,
                uncheckedBorderColor = theme.surfaceBorder,
            ),
        )
    }
}
