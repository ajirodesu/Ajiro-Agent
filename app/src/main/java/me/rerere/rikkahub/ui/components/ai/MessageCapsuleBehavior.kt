package me.rerere.rikkahub.ui.components.ai

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Behaviour values for the message capsule, ported from the HTML capsule spec
 * (lengths converted at 1dp = 3.5px). Visuals (colours, shapes, icons, sizes)
 * are owned by the capsule slots and modifiers, not here.
 */
internal object MessageCapsuleBehavior {
    /** Estimated characters per wrapped line, used to decide single <-> multiline. */
    const val CharsPerLine = 28

    /** Switch to multiline at this many (estimated) lines. */
    const val MultilineAtLines = 2

    /** Show the expand button at this many (estimated) lines. */
    const val ExpandAtLines = 5

    /** Side margin (fraction of available width) while idle. */
    const val IdleMarginFraction = 0.0945f

    /** Side margin while active (focused, or has text). 44px -> ~12.57dp. */
    val ActiveMargin: Dp = (44f / 3.5f).dp

    /** Multiline input auto-grows up to this, then scrolls. 720px -> ~205.7dp. */
    val MaxInputHeight: Dp = (720f / 3.5f).dp

    const val MarginAnimationMs = 220
    const val SizeAnimationMs = 220

    val Easing: Easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
}

/** Hardware Enter sends; Shift+Enter falls through and inserts a newline. */
internal fun Modifier.sendOnHardwareEnter(onSend: () -> Unit): Modifier =
    onPreviewKeyEvent { event ->
        val isEnter = event.key == Key.Enter || event.key == Key.NumPadEnter
        if (isEnter && event.type == KeyEventType.KeyDown && !event.isShiftPressed) {
            onSend()
            true
        } else {
            false
        }
    }
