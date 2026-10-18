package org.inkstack.settings.sources.catalog

import androidx.annotation.WorkerThread
import androidx.lifecycle.viewModelScope
import androidx.room.invalidationTrackerFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import org.inkstack.R
import org.inkstack.core.db.MangaDatabase
import org.inkstack.core.db.TABLE_SOURCES
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.ui.BaseViewModel
import org.inkstack.core.ui.util.ReversibleAction
import org.inkstack.core.util.ext.MutableEventFlow
import org.inkstack.core.util.ext.call
import org.inkstack.core.util.ext.mapSortedByCount
import org.inkstack.explore.data.MangaSourcesRepository
import org.inkstack.explore.data.SourcePresetsRepository
import org.inkstack.explore.data.SourcesSortOrder
import org.inkstack.list.ui.model.ListModel
import org.inkstack.list.ui.model.LoadingState
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.model.MangaSource
import org.inkstack.parsers.util.runCatchingCancellable
import org.inkstack.sourcescore.domain.SourceRanker
import java.util.EnumSet
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SourcesCatalogViewModel @Inject constructor(
	private val repository: MangaSourcesRepository,
	db: MangaDatabase,
	private val settings: AppSettings,
	private val presetsRepository: SourcePresetsRepository,
	private val sourceRanker: SourceRanker,
) : BaseViewModel() {

	val onActionDone = MutableEventFlow<ReversibleAction>()
	val locales: Set<String?> = repository.allMangaSources.mapTo(HashSet<String?>()) { it.locale }.also {
		it.add(null)
	}

	private val searchQuery = MutableStateFlow<String?>(null)
	val appliedFilter = MutableStateFlow(
		SourcesCatalogFilter(
			types = emptySet(),
			locale = Locale.getDefault().language.takeIf { it in locales },
			isNewOnly = false,
		),
	)

	val hasNewSources = repository.observeHasNewSources()
		.stateIn(viewModelScope + Dispatchers.Default, SharingStarted.Lazily, false)

	val contentTypes = MutableStateFlow<List<ContentType>>(emptyList())

	private val activePresetId = settings.activeSourcePresetId
	private val presetSources = MutableStateFlow<Set<String>>(emptySet())

	val isPresetMode: Boolean
		get() = activePresetId != 0L

	val content: StateFlow<List<ListModel>> = combine(
		searchQuery,
		appliedFilter,
		presetSources,
		db.invalidationTrackerFlow(TABLE_SOURCES),
		// Re-rank when a score download lands, not only when the sources table changes.
		sourceRanker.observeScores(),
	) { q, f, ps, _, _ ->
		buildSourcesList(f, q, ps)
	}.stateIn(viewModelScope + Dispatchers.Default, SharingStarted.Eagerly, listOf(LoadingState()))

	init {
		repository.clearNewSourcesBadge()
		launchJob(Dispatchers.Default) {
			contentTypes.value = getContentTypes(settings.isNsfwContentDisabled)
			loadActivePreset()
		}
	}

	fun performSearch(query: String?) {
		searchQuery.value = query?.trim()
	}

	fun setLocale(value: String?) {
		appliedFilter.value = appliedFilter.value.copy(locale = value)
	}

	fun addSource(source: MangaSource) {
		launchJob(Dispatchers.Default) {
			val rollback = repository.setSourcesEnabled(setOf(source), true)
			onActionDone.call(ReversibleAction(R.string.source_enabled, rollback))
		}
	}

	fun togglePresetSource(source: MangaParserSource) {
		if (activePresetId == 0L) return
		launchJob(Dispatchers.Default) {
			val current = presetSources.value.toMutableSet()
			val name = source.name
			if (name in current) {
				current.remove(name)
			} else {
				current.add(name)
			}
			presetsRepository.updatePresetSources(activePresetId, current)
			presetSources.value = current
		}
	}

	fun setContentType(value: ContentType, isAdd: Boolean) {
		val filter = appliedFilter.value
		val types = EnumSet.noneOf(ContentType::class.java)
		types.addAll(filter.types)
		if (isAdd) {
			types.add(value)
		} else {
			types.remove(value)
		}
		appliedFilter.value = filter.copy(types = types)
	}

	fun setNewOnly(value: Boolean) {
		appliedFilter.value = appliedFilter.value.copy(isNewOnly = value)
	}

	private suspend fun buildSourcesList(
		filter: SourcesCatalogFilter,
		query: String?,
		presetSourceNames: Set<String>,
	): List<SourceCatalogItem> {
		val isPreset = activePresetId != 0L
		val sources = repository.queryParserSources(
			isDisabledOnly = !isPreset,
			isNewOnly = filter.isNewOnly,
			excludeBroken = isPreset,
			types = filter.types,
			query = query,
			locale = filter.locale,
			sortOrder = SourcesSortOrder.ALPHABETIC,
		)
		return if (sources.isEmpty()) {
			listOf(
				if (query == null) {
					SourceCatalogItem.Hint(
						icon = R.drawable.ic_empty_feed,
						title = R.string.no_manga_sources,
						text = R.string.no_manga_sources_catalog_text,
					)
				} else {
					SourceCatalogItem.Hint(
						icon = R.drawable.ic_empty_feed,
						title = R.string.nothing_found,
						text = R.string.no_manga_sources_found,
					)
				},
			)
		} else {
			rankForCatalog(sources).map { (source, isHot) ->
				SourceCatalogItem.Source(
					source = source,
					isInPreset = isPreset && source.name in presetSourceNames,
					isHot = isHot,
				)
			}
		}
	}

	/**
	 * The catalogue is where people pick sources to add, so it leads with the ones worth adding:
	 * working before broken, popular (flame-marked) next, then by score. The query is alphabetical,
	 * and the sort is stable, so names still break every tie - and if the community database cannot
	 * be read the list simply stays alphabetical.
	 */
	private suspend fun rankForCatalog(sources: List<MangaParserSource>): List<Pair<MangaParserSource, Boolean>> {
		val snapshot = runCatchingCancellable { sourceRanker.snapshot() }.getOrNull()
			?: return sources.map { it to false }
		val ranks = sources.associate { it.name to snapshot.rank(it) }
		return sources.sortedWith(
			compareBy<MangaParserSource> { it.isBroken }
				.thenByDescending { ranks.getValue(it.name).isHot }
				.thenByDescending { ranks.getValue(it.name).composite },
		).map { it to ranks.getValue(it.name).isHot }
	}

	@WorkerThread
	private fun getContentTypes(isNsfwDisabled: Boolean): List<ContentType> {
		val result = repository.allMangaSources.mapSortedByCount { it.contentType }
		return if (isNsfwDisabled) {
			result.filterNot { it == ContentType.HENTAI }
		} else {
			result
		}
	}

	private suspend fun loadActivePreset() {
		if (activePresetId != 0L) {
			val preset = presetsRepository.getById(activePresetId)
			if (preset != null) {
				presetSources.value = preset.sources
				val presetLocale = preset.languages.firstOrNull()
				if (presetLocale != null && presetLocale in locales) {
					appliedFilter.value = appliedFilter.value.copy(locale = presetLocale)
				}
			}
		}
	}
}
