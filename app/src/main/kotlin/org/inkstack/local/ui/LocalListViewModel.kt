package org.inkstack.local.ui

import android.content.Context
import android.net.Uri
import android.content.SharedPreferences
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import org.inkstack.R
import org.inkstack.core.model.toChipModel
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.parser.MangaDataRepository
import org.inkstack.core.parser.MangaRepository
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.prefs.ListMode
import org.inkstack.core.ui.widgets.ChipsView
import org.inkstack.core.util.ext.MutableEventFlow
import org.inkstack.core.util.ext.call
import org.inkstack.core.util.ext.toFileOrNull
import org.inkstack.core.util.ext.toUriOrNull
import org.inkstack.explore.data.MangaSourcesRepository
import org.inkstack.explore.domain.ExploreRepository
import org.inkstack.filter.ui.FilterCoordinator
import org.inkstack.list.domain.ListFilterOption
import org.inkstack.list.domain.MangaListMapper
import org.inkstack.list.domain.QuickFilterListener
import org.inkstack.list.ui.model.EmptyState
import org.inkstack.list.ui.model.ListModel
import org.inkstack.list.ui.model.MangaListModel
import org.inkstack.list.ui.model.QuickFilter
import org.inkstack.list.ui.model.TipModel
import org.inkstack.local.data.LocalStorageChanges
import org.inkstack.local.data.LocalStorageManager
import org.inkstack.local.domain.DeleteLocalMangaUseCase
import org.inkstack.local.domain.model.LocalManga
import org.inkstack.parsers.model.Manga
import org.inkstack.remotelist.ui.RemoteListViewModel
import javax.inject.Inject

@HiltViewModel
class LocalListViewModel @Inject constructor(
	savedStateHandle: SavedStateHandle,
	mangaRepositoryFactory: MangaRepository.Factory,
	filterCoordinator: FilterCoordinator,
	private val settings: AppSettings,
	mangaListMapper: MangaListMapper,
	private val deleteLocalMangaUseCase: DeleteLocalMangaUseCase,
	exploreRepository: ExploreRepository,
	@param:LocalStorageChanges private val localStorageChanges: SharedFlow<LocalManga?>,
	private val localStorageManager: LocalStorageManager,
	sourcesRepository: MangaSourcesRepository,
	mangaDataRepository: MangaDataRepository,
	@ApplicationContext appContext: Context,
) : RemoteListViewModel(
	savedStateHandle = savedStateHandle,
	mangaRepositoryFactory = mangaRepositoryFactory,
	filterCoordinator = filterCoordinator,
	settings = settings,
	mangaListMapper = mangaListMapper,
	exploreRepository = exploreRepository,
	sourcesRepository = sourcesRepository,
	mangaDataRepository = mangaDataRepository,
	appContext = appContext,
	localStorageChanges = localStorageChanges,
), SharedPreferences.OnSharedPreferenceChangeListener, QuickFilterListener {

	val onMangaRemoved = MutableEventFlow<Unit>()
	private val showInlineFilter: Boolean = savedStateHandle[AppRouter.KEY_IS_BOTTOMTAB] ?: false

	init {
		launchJob(Dispatchers.Default) {
			localStorageChanges
				.collect {
					loadList(filterCoordinator.snapshot(), append = false).join()
				}
		}
		settings.subscribe(this)
	}

	override suspend fun onBuildList(list: MutableList<ListModel>) {
		super.onBuildList(list)
		if (showInlineFilter) {
			createFilterHeader(maxCount = 16)?.let {
				list.add(0, it)
			}
		}
		if (!localStorageManager.hasExternalStoragePermission(isReadOnly = true)) {
			for (item in list) {
				if (item !is MangaListModel) {
					continue
				}
				val file = item.manga.url.toUriOrNull()?.toFileOrNull() ?: continue
				if (localStorageManager.isOnExternalStorage(file)) {
					val tip = TipModel(
						key = "permission",
						title = R.string.external_storage,
						text = R.string.missing_storage_permission,
						icon = R.drawable.ic_storage,
						primaryButtonText = R.string.pick_custom_directory,
						secondaryButtonText = R.string.settings,
					)
					list.add(0, tip)
					return
				}
			}
		}
	}

	override fun setFilterOption(option: ListFilterOption, isApplied: Boolean) {
		if (option is ListFilterOption.Tag) {
			filterCoordinator.toggleTag(option.tag, isApplied)
		}
	}

	override fun toggleFilterOption(option: ListFilterOption) {
		if (option is ListFilterOption.Tag) {
			val tag = option.tag
			val isSelected = tag in filterCoordinator.snapshot().listFilter.tags
			filterCoordinator.toggleTag(option.tag, !isSelected)
		}
	}

	override fun clearFilter() = filterCoordinator.reset()

	override fun onCleared() {
		settings.unsubscribe(this)
		super.onCleared()
	}

	override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
		if (key == AppSettings.KEY_LOCAL_MANGA_DIRS) {
			onRefresh()
		}
	}

	fun onCustomDirectoryPicked(uri: Uri) {
		launchLoadingJob(Dispatchers.Default) {
			localStorageManager.takePermissions(uri)
			val dir = localStorageManager.resolveUri(uri)
			if (!dir.canRead()) {
				return@launchLoadingJob
			}
			if (dir !in localStorageManager.getApplicationStorageDirs()) {
				settings.addUserSpecifiedMangaDirectory(dir)
			}
			onRefresh()
		}
	}

	fun delete(ids: Set<Long>) {
		launchLoadingJob(Dispatchers.Default) {
			deleteLocalMangaUseCase(ids)
			onMangaRemoved.call(Unit)
		}
	}

	override suspend fun mapMangaList(
		destination: MutableCollection<in ListModel>,
		manga: Collection<Manga>,
		mode: ListMode
	) = mangaListMapper.toListModelList(destination, manga, mode, MangaListMapper.NO_SAVED)

	override fun createEmptyState(canResetFilter: Boolean): EmptyState = if (canResetFilter) {
		super.createEmptyState(true)
	} else {
		EmptyState(
			icon = R.drawable.ic_empty_local,
			textPrimary = R.string.text_local_holder_primary,
			textSecondary = R.string.text_local_holder_secondary,
			actionStringRes = R.string._import,
		)
	}

	private suspend fun createFilterHeader(maxCount: Int): QuickFilter? {
		val appliedTags = filterCoordinator.snapshot().listFilter.tags
		val availableTags = repository.getFilterOptions().availableTags
		if (appliedTags.isEmpty() && availableTags.size < 3) {
			return null
		}
		val result = ArrayList<ChipsView.ChipModel>(minOf(availableTags.size, maxCount))
		appliedTags.mapTo(result) { tag ->
			ListFilterOption.Tag(tag).toChipModel(isChecked = true)
		}
		for (tag in availableTags) {
			if (result.size >= maxCount) {
				break
			}
			if (tag in appliedTags) {
				continue
			}
			result.add(ListFilterOption.Tag(tag).toChipModel(isChecked = false))
		}
		return QuickFilter(result)
	}
}
