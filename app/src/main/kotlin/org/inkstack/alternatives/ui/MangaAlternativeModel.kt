package org.inkstack.alternatives.ui

import org.inkstack.core.model.chaptersCount
import org.inkstack.list.ui.model.ListModel
import org.inkstack.list.ui.model.MangaGridModel
import org.inkstack.parsers.model.Manga

data class MangaAlternativeModel(
	val mangaModel: MangaGridModel,
	private val referenceChapters: Int,
	val isMigrationEnabled: Boolean = true,
) : ListModel {

	val manga: Manga
		get() = mangaModel.manga

	val chaptersCount = manga.chaptersCount()

	val chaptersDiff: Int
		get() = if (referenceChapters == 0 || chaptersCount == 0) 0 else chaptersCount - referenceChapters

	override fun areItemsTheSame(other: ListModel): Boolean {
		return other is MangaAlternativeModel && other.manga.id == manga.id
	}

	override fun getChangePayload(previousState: ListModel): Any? = if (previousState is MangaAlternativeModel) {
		mangaModel.getChangePayload(previousState.mangaModel)
	} else {
		null
	}
}

data class MangaAlternativeReference(
	val manga: Manga,
	val chaptersCount: Int = manga.chaptersCount(),
)
