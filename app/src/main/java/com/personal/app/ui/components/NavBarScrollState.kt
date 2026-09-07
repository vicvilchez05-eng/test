package com.personal.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

/**
 * Watches scroll direction anywhere below it in the tree and decides whether the bubble bar
 * should be collapsed. Install with `Modifier.nestedScroll(state.connection)` on a parent of
 * every scrollable screen; no screen needs to know about the bar.
 *
 * Only scroll the content actually consumed counts (a swipe on a list that can't move does
 * nothing). Direction changes are debounced by [thresholdPx] of accumulated travel so a finger
 * tremor doesn't flicker the bar. Reaching the top of the content always re-expands it.
 */
class NavBarScrollState(private val thresholdPx: Float) {
    var collapsed by mutableStateOf(false)
        private set

    private var accumulated = 0f

    fun expand() {
        collapsed = false
        accumulated = 0f
    }

    val connection: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            // Content couldn't scroll further up: we're at the top, show the full bar.
            if (available.y > 0 && consumed.y == 0f && source == NestedScrollSource.UserInput) {
                expand()
                return Offset.Zero
            }
            val dy = consumed.y
            if (dy == 0f) return Offset.Zero
            // Reset when direction flips so the threshold measures a continuous gesture.
            if ((dy < 0 && accumulated > 0) || (dy > 0 && accumulated < 0)) accumulated = 0f
            accumulated += dy
            if (accumulated < -thresholdPx && !collapsed) collapsed = true
            if (accumulated > thresholdPx && collapsed) collapsed = false
            return Offset.Zero
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            if (available.y > 0) expand()
            return Velocity.Zero
        }
    }
}

@Composable
fun rememberNavBarScrollState(thresholdPx: Float = 24f): NavBarScrollState =
    remember { NavBarScrollState(thresholdPx) }
