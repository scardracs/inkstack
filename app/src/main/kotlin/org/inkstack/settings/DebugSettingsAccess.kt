package org.inkstack.settings

import androidx.compose.runtime.Composable

/**
 * Gate for flavor-only debug settings UI (debug/nightly).
 * Release builds leave [content] null.
 */
object DebugSettingsAccess {

	@Volatile
	var content: (@Composable (onBack: () -> Unit) -> Unit)? = null
		private set

	val isAvailable: Boolean
		get() = content != null

	fun register(content: @Composable (onBack: () -> Unit) -> Unit) {
		this.content = content
	}
}
