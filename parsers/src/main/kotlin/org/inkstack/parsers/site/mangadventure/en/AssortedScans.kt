package org.inkstack.parsers.site.mangadventure.en

import androidx.collection.ArraySet
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaListFilterOptions
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangadventure.MangAdventureParser

@MangaSourceParser("ASSORTEDSCANS", "AssortedScans", "en")
internal class AssortedScans(context: MangaLoaderContext) :
	MangAdventureParser(context, MangaParserSource.ASSORTEDSCANS, "assortedscans.com") {
	// tags that don't have any series and make the tests fail
	private val emptyTags = setOf(
		"Doujinshi", "Harem", "Hentai", "Mecha",
		"Shoujo Ai", "Shounen Ai", "Smut", "Yaoi",
	)

	override suspend fun getFilterOptions(): MangaListFilterOptions {
		val options = super.getFilterOptions()
		return options.copy(
			availableTags = options.availableTags.filterNotTo(ArraySet(options.availableTags.size)) { it.key in emptyTags },
		)
	}
}
