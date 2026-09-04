package com.esforal.gamelauncher.presentation.launcher

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.esforal.gamelauncher.domain.model.GamePage

/**
 * Arrastrar de lado cambia de juego, de uno en uno.
 *
 * Es el equivalente tactil de la rueda del raton en las dos disposiciones que
 * vienen del escritorio. Alli las filas tienen el desplazamiento desactivado
 * —el centro lo decide la seleccion, no el dedo— y la rueda mueve la seleccion
 * con un respiro entre pasos para que un tiron no salte tres juegos. Aqui el
 * respiro es una distancia en vez de un tiempo: [stepSize] por juego.
 *
 * Vive aparte y no dentro de cada disposicion porque las dos lo necesitan
 * identico. Es la misma linea que sigue el resto del launcher: se comparte el
 * componente y se duplica solo el reparto de la pantalla.
 *
 * **Dos cosas que parecen detalles y no lo son.**
 *
 * `pointerInput` va con clave `Unit` y no con la seleccion actual. Con la
 * seleccion de clave, la primera vez que el arrastre cambia de juego el bloque
 * se reinicia y **cancela el gesto en curso**: el dedo sigue en la pantalla y
 * ya no hace nada hasta levantarlo. Los valores de fuera se leen frescos con
 * `rememberUpdatedState`, que es justo para esto.
 *
 * El indice se lleva en una variable local del gesto ([cursor]) en vez de
 * releerlo en cada paso. Dentro de un mismo evento de arrastre no ha habido
 * recomposicion todavia, asi que el indice de fuera aun es el viejo: releyendo,
 * un arrastre largo elegiria el mismo vecino una y otra vez en lugar de
 * avanzar.
 */
@Composable
internal fun Modifier.selectionDrag(
    pages: List<GamePage>,
    index: Int,
    onSelect: (GamePage) -> Unit,
    stepSize: Dp = SELECTION_DRAG_STEP,
): Modifier {
    val currentPages by rememberUpdatedState(pages)
    val currentIndex by rememberUpdatedState(index)
    val currentOnSelect by rememberUpdatedState(onSelect)

    return pointerInput(Unit) {
        var travelled = 0f
        var cursor = 0

        detectHorizontalDragGestures(
            onDragStart = {
                cursor = currentIndex
                travelled = 0f
            },
            onDragEnd = { travelled = 0f },
            onDragCancel = { travelled = 0f },
        ) { _, delta ->
            travelled += delta
            val step = stepSize.toPx()

            // Se avanza un juego por cada paso completo, y lo que sobra se
            // guarda para el siguiente evento: asi un arrastre lento y uno
            // rapido recorren lo mismo.
            while (travelled <= -step) {
                travelled += step
                currentPages.getOrNull(cursor + 1)?.let {
                    cursor += 1
                    currentOnSelect(it)
                }
            }
            while (travelled >= step) {
                travelled -= step
                currentPages.getOrNull(cursor - 1)?.let {
                    cursor -= 1
                    currentOnSelect(it)
                }
            }
        }
    }
}

/**
 * Cuanto hay que arrastrar para pasar de juego.
 *
 * Por debajo de esto un desplazamiento involuntario del pulgar mientras se
 * pulsa una portada cambiaria de juego; muy por encima, el gesto se siente
 * pesado. Sale de la holgura tactil habitual, no de una medida de la pantalla,
 * asi que no escala con el alto como el resto.
 */
private val SELECTION_DRAG_STEP = 64.dp
