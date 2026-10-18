package org.inkstack.history.data

import dagger.Reusable
import org.inkstack.core.db.MangaDatabase
import org.inkstack.core.db.entity.toManga
import org.inkstack.core.db.entity.toMangaTags
import org.inkstack.history.domain.model.MangaWithHistory
import org.inkstack.list.domain.ListFilterOption
import org.inkstack.list.domain.ListSortOrder
import org.inkstack.local.data.index.LocalMangaIndex
import org.inkstack.local.domain.LocalObserveMapper
import org.inkstack.parsers.model.Manga
import javax.inject.Inject

@Reusable
class HistoryLocalObserver @Inject constructor(
	localMangaIndex: LocalMangaIndex,
	private val db: MangaDatabase,
) : LocalObserveMapper<HistoryWithManga, MangaWithHistory>(localMangaIndex) {

	fun observeAll(
		order: ListSortOrder,
		filterOptions: Set<ListFilterOption>,
		limit: Int
	) = db.getHistoryDao().observeAll(order, filterOptions, limit).mapToLocal()

	override fun toManga(e: HistoryWithManga) = e.manga.toManga(e.tags.toMangaTags(), null)

	override fun toResult(e: HistoryWithManga, manga: Manga) = MangaWithHistory(
		manga = manga,
		history = e.history.toMangaHistory(),
	)
}
