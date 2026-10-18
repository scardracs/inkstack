package org.inkstack.favourites.domain

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import org.inkstack.core.db.MangaDatabase
import org.inkstack.core.db.entity.toManga
import org.inkstack.core.db.entity.toMangaTags
import org.inkstack.favourites.data.FavouriteManga
import org.inkstack.list.domain.ListFilterOption
import org.inkstack.list.domain.ListSortOrder
import org.inkstack.local.data.index.LocalMangaIndex
import org.inkstack.local.domain.LocalObserveMapper
import org.inkstack.parsers.model.Manga
import javax.inject.Inject

@Reusable
class LocalFavoritesObserver @Inject constructor(
	localMangaIndex: LocalMangaIndex,
	private val db: MangaDatabase,
) : LocalObserveMapper<FavouriteManga, Manga>(localMangaIndex) {

	fun observeAll(
		order: ListSortOrder,
		filterOptions: Set<ListFilterOption>,
		limit: Int
	): Flow<List<Manga>> = db.getFavouritesDao().observeAll(order, filterOptions, limit).mapToLocal()

	fun observeAll(
		categoryId: Long,
		order: ListSortOrder,
		filterOptions: Set<ListFilterOption>,
		limit: Int
	): Flow<List<Manga>> = db.getFavouritesDao().observeAll(categoryId, order, filterOptions, limit).mapToLocal()

	override fun toManga(e: FavouriteManga) = e.manga.toManga(e.tags.toMangaTags(), null)

	override fun toResult(e: FavouriteManga, manga: Manga) = manga
}
