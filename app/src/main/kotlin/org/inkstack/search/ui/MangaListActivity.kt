package org.inkstack.search.ui

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import coil3.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.inkstack.core.model.LocalMangaSource
import org.inkstack.core.model.MangaSource
import org.inkstack.core.model.isNsfw
import org.inkstack.core.model.parcelable.ParcelableMangaListFilter
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.core.util.ext.getParcelableExtraCompat
import org.inkstack.core.util.ext.getSerializableExtraCompat
import org.inkstack.filter.ui.FilterCoordinator
import org.inkstack.filter.ui.FilterHeaderProducer
import org.inkstack.local.ui.LocalListViewModel
import org.inkstack.parsers.model.Manga
import org.inkstack.parsers.model.MangaSource
import org.inkstack.parsers.model.SortOrder
import org.inkstack.remotelist.ui.RemoteListViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MangaListActivity :
	BaseComposeActivity(),
	FilterCoordinator.Owner {

	@Inject
	lateinit var imageLoader: ImageLoader

	@Inject
	lateinit var filterHeaderProducer: FilterHeaderProducer

	private lateinit var source: MangaSource

	private val listViewModel: RemoteListViewModel by lazy {
		val clazz = if (source == LocalMangaSource) {
			LocalListViewModel::class.java
		} else {
			RemoteListViewModel::class.java
		}
		ViewModelProvider(this, defaultViewModelProviderFactory)[clazz]
	}

	override val filterCoordinator: FilterCoordinator
		get() = listViewModel.filterCoordinator

	override fun onCreate(savedInstanceState: Bundle?) {
		val sourceName = intent.getStringExtra(AppRouter.KEY_SOURCE)
		source = MangaSource(sourceName)

		super.onCreate(savedInstanceState)
		listViewModel

		val filter = intent.getParcelableExtraCompat<ParcelableMangaListFilter>(AppRouter.KEY_FILTER)?.filter
		val sortOrder = intent.getSerializableExtraCompat<SortOrder>(AppRouter.KEY_SORT_ORDER)

		setComposeContent {
			MangaListScreen(
				isLocal = source == LocalMangaSource,
				imageLoader = imageLoader,
				filterHeaderProducer = filterHeaderProducer,
				exceptionResolver = exceptionResolver,
				initialFilter = filter,
				initialSortOrder = sortOrder,
				onBack = { dispatchNavigateUp() },
			)
		}
	}

	override fun isNsfwContent(): Flow<Boolean> = flowOf(source.isNsfw())

	fun setFilterHeaderVisible(isVisible: Boolean) = Unit

	fun showPreview(manga: Manga): Boolean = false

	fun hidePreview() = Unit
}
