package org.inkstack.parsers.site.mangareader.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaListFilterCapabilities
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@Broken
@MangaSourceParser("MANHWALOVER", "ManhwaLover", "en", ContentType.HENTAI)
internal class ManhwaLover(context: MangaLoaderContext) :
	MangaReaderParser(
		context,
		MangaParserSource.MANHWALOVER,
		"www.manhwalover.com",
		pageSize = 20,
		searchPageSize = 20,
	) {
	override val datePattern = "MMM d, yyyy"
	override val filterCapabilities: MangaListFilterCapabilities
		get() = super.filterCapabilities.copy(
			isTagsExclusionSupported = false,
		)
}
