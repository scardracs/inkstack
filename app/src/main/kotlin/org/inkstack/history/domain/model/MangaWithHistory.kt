package org.inkstack.history.domain.model

import org.inkstack.core.model.MangaHistory
import org.inkstack.parsers.model.Manga

data class MangaWithHistory(
	val manga: Manga,
	val history: MangaHistory
)
