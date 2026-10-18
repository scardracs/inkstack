package org.inkstack.list.ui.model

import org.inkstack.core.ui.model.MangaOverride
import org.inkstack.parsers.model.Manga

data class MangaCompactListModel(
	override val manga: Manga,
	override val override: MangaOverride?,
	val subtitle: String,
	override val counter: Int,
) : MangaListModel()
