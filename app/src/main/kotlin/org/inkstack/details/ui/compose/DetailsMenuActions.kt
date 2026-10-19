package org.inkstack.details.ui.compose

data class DetailsMenuVisibility(
	val share: Boolean = false,
	val download: Boolean = false,
	val delete: Boolean = false,
	val browser: Boolean = false,
	val alternatives: Boolean = false,
	val replaceSource: Boolean = false,
	val shortcut: Boolean = false,
	val scrobbling: Boolean = false,
	val online: Boolean = false,
	val stats: Boolean = false,
	val editOverride: Boolean = true,
	val findSimilar: Boolean = true,
	val chaptersSheet: Boolean = true,
)

data class DetailsReadMenuVisibility(
	val incognito: Boolean = false,
	val forget: Boolean = false,
	val download: Boolean = false,
)
