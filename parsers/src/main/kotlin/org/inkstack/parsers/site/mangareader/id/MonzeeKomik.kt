package org.inkstack.parsers.site.mangareader.id

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaListFilterCapabilities
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser
import java.util.*

@Broken
@MangaSourceParser("MONZEEKOMIK", "MonzeeKomik", "id")
internal class MonzeeKomik(context: MangaLoaderContext) :
	MangaReaderParser(
		context,
		MangaParserSource.MONZEEKOMIK,
		"monzee01.my.id",
		pageSize = 30,
		searchPageSize = 10,
	) {
	override val sourceLocale: Locale = Locale.ENGLISH
	override val filterCapabilities: MangaListFilterCapabilities
		get() = super.filterCapabilities.copy(
			isTagsExclusionSupported = false,
		)
}
