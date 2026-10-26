package org.inkstack.core.ui.compose

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Default floating chrome shape — pill / rounded-xl. */
val InkstackGlassShape: Shape = RoundedCornerShape(28.dp)

/** Near-white / near-black foregrounds — pick from the glass fill luminance, not theme mode. */
private val GlassOnDarkFill = Color(0xFFF2F2F2)
private val GlassOnLightFill = Color(0xFF1C1B1F)

/**
 * Content color for controls drawn on [InkstackGlassSurface].
 *
 * Uses [ColorScheme.surfaceContainerHigh] luminance (the glass fill), not [ColorScheme.surface]
 * or [isSystemInDarkTheme]: Material You dark schemes often use a light container tint, so
 * “white because dark theme” made icons disappear into the pill.
 */
@Composable
fun inkstackGlassContentColor(): Color {
	val fill = MaterialTheme.colorScheme.surfaceContainerHigh
	return if (fill.luminance() > 0.5f) GlassOnLightFill else GlassOnDarkFill
}

/**
 * Muted secondary label on glass (subtitles, placeholders).
 */
@Composable
fun inkstackGlassContentColorMuted(): Color {
	return inkstackGlassContentColor().copy(alpha = 0.72f)
}

/**
 * Shared chrome surface matching Google's frosted-glass look (Android 17-style):
 * Dynamic Color tint at semi-transparent alpha + thin border.
 *
 * No [Modifier.shadow]: elevation shadows under a translucent fill read as a mid-bar
 * horizontal seam (dark and light themes). Depth comes from border + tint only.
 *
 * In dark theme, children inherit a light [LocalContentColor] so icons/text stay
 * chiaro on the charcoal frost. Children are never blurred.
 */
@Composable
fun InkstackGlassSurface(
	modifier: Modifier = Modifier,
	shape: Shape = InkstackGlassShape,
	alpha: Float = 0.82f,
	@Suppress("UNUSED_PARAMETER") elevation: Dp = 0.dp,
	borderWidth: Dp = 1.dp,
	enabledBlur: Boolean = true,
	content: @Composable BoxScope.() -> Unit,
) {
	val scheme = MaterialTheme.colorScheme
	val fill = scheme.surfaceContainerHigh
	val fillIsLight = fill.luminance() > 0.5f
	val reduceTransparency = rememberReduceTransparency()
	val blurCapable = enabledBlur &&
		!reduceTransparency &&
		Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
	val effectiveAlpha = when {
		blurCapable -> alpha.coerceIn(0.72f, 0.92f)
		else -> (alpha + 0.10f).coerceIn(0.82f, 0.96f)
	}
	// Flat surfaceContainer tint — surfaceColorAtElevation adds a tonal band that
	// also reads as a center streak on pill chrome.
	val tint = remember(fill, effectiveAlpha) {
		fill.copy(alpha = effectiveAlpha)
	}
	val borderColor = remember(scheme.outlineVariant, fillIsLight, blurCapable) {
		when {
			!fillIsLight -> GlassOnDarkFill.copy(alpha = 0.18f)
			blurCapable -> scheme.outlineVariant.copy(alpha = 0.40f)
			else -> scheme.outlineVariant.copy(alpha = 0.28f)
		}
	}
	val contentColor = inkstackGlassContentColor()

	CompositionLocalProvider(LocalContentColor provides contentColor) {
		Box(
			modifier = modifier
				.clip(shape)
				.background(color = tint)
				.border(width = borderWidth, color = borderColor, shape = shape),
			content = content,
		)
	}
}

@Composable
private fun rememberReduceTransparency(): Boolean {
	val context = LocalContext.current
	return remember(context) {
		runCatching {
			android.provider.Settings.Global.getFloat(
				context.contentResolver,
				"accessibility_reduce_transparency",
				0f,
			) > 0f
		}.getOrDefault(false)
	}
}

/** Convenience glass fill for places that cannot wrap in [InkstackGlassSurface]. */
@Composable
fun inkstackGlassTint(alpha: Float = 0.82f): Color {
	return MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp).copy(alpha = alpha)
}
