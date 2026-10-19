package org.inkstack.core.ui.compose

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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

/** Near-white foreground for icons/labels on dark frosted chrome (chiaro, not grey-on-charcoal). */
private val GlassDarkContent = Color(0xFFF2F2F2)

/**
 * Content color for controls drawn on [InkstackGlassSurface].
 * Dark theme: forced light/white-ish. Light theme: theme [onSurface].
 */
@Composable
fun inkstackGlassContentColor(): Color {
	val scheme = MaterialTheme.colorScheme
	val isDark = isSystemInDarkTheme() || scheme.surface.luminance() < 0.5f
	return if (isDark) GlassDarkContent else scheme.onSurface
}

/**
 * Muted secondary label on glass (subtitles, placeholders) — still light in dark theme.
 */
@Composable
fun inkstackGlassContentColorMuted(): Color {
	val scheme = MaterialTheme.colorScheme
	val isDark = isSystemInDarkTheme() || scheme.surface.luminance() < 0.5f
	return if (isDark) GlassDarkContent.copy(alpha = 0.72f) else scheme.onSurfaceVariant
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
	val isDark = isSystemInDarkTheme() || scheme.surface.luminance() < 0.5f
	val reduceTransparency = rememberReduceTransparency()
	val blurCapable = enabledBlur &&
		!reduceTransparency &&
		Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
	val effectiveAlpha = when {
		blurCapable -> alpha.coerceIn(0.68f, 0.88f)
		else -> (alpha + 0.08f).coerceIn(0.78f, 0.94f)
	}
	// Flat surfaceContainer tint — surfaceColorAtElevation adds a tonal band that
	// also reads as a center streak on pill chrome.
	val tint = remember(scheme.surfaceContainerHigh, effectiveAlpha) {
		scheme.surfaceContainerHigh.copy(alpha = effectiveAlpha)
	}
	val borderColor = remember(scheme.outlineVariant, isDark, blurCapable) {
		when {
			isDark -> GlassDarkContent.copy(alpha = 0.18f)
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
