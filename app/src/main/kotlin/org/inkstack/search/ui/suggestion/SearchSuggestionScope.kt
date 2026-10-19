package org.inkstack.search.ui.suggestion

/**
 * Scope for optional live filtering of the current main tab while typing in the shared search bar.
 * Submitting search always opens the global multi-source search from every tab.
 */
enum class SearchSuggestionScope {

	ALL,
	HISTORY,
	FAVOURITES,
}
