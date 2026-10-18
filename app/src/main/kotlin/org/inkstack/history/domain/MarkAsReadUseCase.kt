package org.inkstack.history.domain

import dagger.Reusable
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.inkstack.core.model.getPreferredBranch
import org.inkstack.core.parser.MangaRepository
import org.inkstack.history.data.HistoryRepository
import org.inkstack.parsers.model.Manga
import javax.inject.Inject

@Reusable
class MarkAsReadUseCase @Inject constructor(
	private val historyRepository: HistoryRepository,
	private val mangaRepositoryFactory: MangaRepository.Factory,
) {

	suspend operator fun invoke(manga: Manga) {
		val repo = mangaRepositoryFactory.create(manga.source)
		val details = if (manga.chapters.isNullOrEmpty()) {
			repo.getDetails(manga)
		} else {
			manga
		}
		val history = historyRepository.getOne(details)
		val branch = details.getPreferredBranch(history)
		val lastChapter = details.getChapters(branch).lastOrNull() ?: return
		val pages = repo.getPages(lastChapter)
		historyRepository.addOrUpdate(
			manga = details,
			chapterId = lastChapter.id,
			page = pages.lastIndex,
			scroll = 0,
			percent = 1f,
			force = true,
		)
	}

	suspend operator fun invoke(manga: Collection<Manga>) {
		when (manga.size) {
			0 -> Unit
			1 -> invoke(manga.first())
			else -> supervisorScope {
				manga.map {
					launch {
						invoke(it)
					}
				}.joinAll()
			}
		}
	}
}
