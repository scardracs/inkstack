package org.inkstack.picker.ui.manga

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import org.inkstack.R
import org.inkstack.core.parser.MangaDataRepository
import org.inkstack.core.prefs.AppSettings
import org.inkstack.favourites.domain.FavouritesRepository
import org.inkstack.history.data.HistoryRepository
import org.inkstack.list.domain.MangaListMapper
import org.inkstack.list.ui.MangaListViewModel
import org.inkstack.list.ui.model.ListHeader
import org.inkstack.list.ui.model.ListModel
import org.inkstack.list.ui.model.LoadingState
import javax.inject.Inject
import kotlinx.coroutines.flow.SharedFlow
import org.inkstack.local.data.LocalStorageChanges
import org.inkstack.local.domain.model.LocalManga

@HiltViewModel
class MangaPickerViewModel @Inject constructor(
	private val settings: AppSettings,
	mangaDataRepository: MangaDataRepository,
	private val historyRepository: HistoryRepository,
	private val favouritesRepository: FavouritesRepository,
	private val mangaListMapper: MangaListMapper,
	@LocalStorageChanges localStorageChanges: SharedFlow<LocalManga?>,
) : MangaListViewModel(settings, mangaDataRepository, localStorageChanges) {

	override val content: StateFlow<List<ListModel>> = flow {
		emit(loadList())
	}.stateIn(viewModelScope + Dispatchers.Default, SharingStarted.Lazily, listOf(LoadingState()))

	override fun onRefresh() = Unit

	override fun onRetry() = Unit

	private suspend fun loadList() = buildList {
		val history = historyRepository.getList(0, Int.MAX_VALUE)
		if (history.isNotEmpty()) {
			add(ListHeader(R.string.history))
			mangaListMapper.toListModelList(this, history, settings.listMode)
		}
		val categories = favouritesRepository.observeCategoriesForLibrary().first()
		for (category in categories) {
			val favorites = favouritesRepository.getManga(category.id)
			if (favorites.isNotEmpty()) {
				add(ListHeader(category.title))
				mangaListMapper.toListModelList(this, favorites, settings.listMode)
			}
		}
	}
}
