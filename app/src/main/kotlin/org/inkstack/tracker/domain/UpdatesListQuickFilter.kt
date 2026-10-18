package org.inkstack.tracker.domain

import org.inkstack.core.prefs.AppSettings
import org.inkstack.favourites.domain.FavouritesRepository
import org.inkstack.list.domain.ListFilterOption
import org.inkstack.list.domain.MangaListQuickFilter
import javax.inject.Inject

class UpdatesListQuickFilter @Inject constructor(
	private val favouritesRepository: FavouritesRepository,
	settings: AppSettings,
) : MangaListQuickFilter(settings) {

	override suspend fun getAvailableFilterOptions(): List<ListFilterOption> =
		favouritesRepository.getMostUpdatedCategories(
			limit = 4,
		).map {
			ListFilterOption.Favorite(it)
		}
}
