package org.inkstack.core.ui.compose

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import org.inkstack.R
import org.inkstack.main.ui.nav.composeColorSchemeFromTheme

@OptIn(ExperimentalTextApi::class)
private val GoogleSans: FontFamily
	@Composable
	get() = remember {
		FontFamily(
			Font(R.font.google_sans, weight = FontWeight.Normal, variationSettings = weightOf(400)),
			Font(R.font.google_sans, weight = FontWeight.Medium, variationSettings = weightOf(500)),
			Font(R.font.google_sans, weight = FontWeight.SemiBold, variationSettings = weightOf(600)),
			Font(R.font.google_sans, weight = FontWeight.Bold, variationSettings = weightOf(700)),
		)
	}

private fun weightOf(weight: Int) = FontVariation.Settings(FontVariation.weight(weight))

@Composable
private fun inkstackTypography(family: FontFamily): Typography {
	val noPadding = PlatformTextStyle(includeFontPadding = false)
	val base = Typography()
	return base.copy(
		displayLarge = base.displayLarge.copy(fontFamily = family, platformStyle = noPadding),
		displayMedium = base.displayMedium.copy(fontFamily = family, platformStyle = noPadding),
		displaySmall = base.displaySmall.copy(fontFamily = family, platformStyle = noPadding),
		headlineLarge = base.headlineLarge.copy(fontFamily = family, platformStyle = noPadding),
		headlineMedium = base.headlineMedium.copy(fontFamily = family, platformStyle = noPadding),
		headlineSmall = base.headlineSmall.copy(fontFamily = family, platformStyle = noPadding),
		titleLarge = base.titleLarge.copy(fontFamily = family, platformStyle = noPadding),
		titleMedium = base.titleMedium.copy(fontFamily = family, platformStyle = noPadding),
		titleSmall = base.titleSmall.copy(fontFamily = family, platformStyle = noPadding),
		bodyLarge = base.bodyLarge.copy(fontFamily = family, platformStyle = noPadding),
		bodyMedium = base.bodyMedium.copy(fontFamily = family, platformStyle = noPadding),
		bodySmall = base.bodySmall.copy(fontFamily = family, platformStyle = noPadding),
		labelLarge = base.labelLarge.copy(fontFamily = family, fontWeight = FontWeight.Bold, platformStyle = noPadding),
		labelMedium = base.labelMedium.copy(fontFamily = family, fontWeight = FontWeight.Bold, platformStyle = noPadding),
		labelSmall = base.labelSmall.copy(fontFamily = family, fontWeight = FontWeight.Medium, platformStyle = noPadding),
	)
}

/**
 * App-wide Material3 theme that inherits the host Activity colour scheme
 * (Monet / named / AMOLED) via [composeColorSchemeFromTheme].
 */
@Composable
fun InkstackTheme(content: @Composable () -> Unit) {
	val context = LocalContext.current
	val isDark = isSystemInDarkTheme() ||
		(LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
		Configuration.UI_MODE_NIGHT_YES
	val scheme = remember(context.theme, isDark) { composeColorSchemeFromTheme(context, isDark) }
	MaterialTheme(
		colorScheme = scheme,
		typography = inkstackTypography(GoogleSans),
		content = content,
	)
}
