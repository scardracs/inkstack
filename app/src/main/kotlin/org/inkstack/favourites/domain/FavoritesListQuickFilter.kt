package org.inkstack.favourites.domain

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.inkstack.core.os.NetworkState
import org.inkstack.core.prefs.AppSettings
import org.inkstack.list.domain.ListFilterOption
import org.inkstack.list.domain.MangaListQuickFilter
import org.inkstack.parsers.model.ContentType

class FavoritesListQuickFilter @AssistedInject constructor(
	@Assisted private val categoryId: Long,
	private val settings: AppSettings,
	private val repository: FavouritesRepository,
	networkState: NetworkState,
) : MangaListQuickFilter(settings) {

	init {
		setFilterOption(ListFilterOption.Downloaded, !networkState.value)
	}

	override suspend fun getAvailableFilterOptions(): List<ListFilterOption> = buildList {
		add(ListFilterOption.Downloaded)
		if (settings.isTrackerEnabled) {
			add(ListFilterOption.Macro.NEW_CHAPTERS)
		}
		add(ListFilterOption.Macro.COMPLETED)
		add(ListFilterOption.ContentType(ContentType.MANGA))
		add(ListFilterOption.ContentType(ContentType.MANHWA))
		add(ListFilterOption.ContentType(ContentType.MANHUA))
		repository.findPopularTagTitles(categoryId, 3).mapTo(this) {
			ListFilterOption.TagTitle(it)
		}
		repository.findPopularSources(categoryId, 3).mapTo(this) {
			ListFilterOption.Source(it)
		}
	}

	@AssistedFactory
	interface Factory {

		fun create(categoryId: Long): FavoritesListQuickFilter
	}
}
