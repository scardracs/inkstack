package org.inkstack.parsers.site.mangareader.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaListFilterCapabilities
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@MangaSourceParser("ARCURAFANSUB", "ArcuraFansub", "tr", ContentType.HENTAI)
internal class ArcuraFansub(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.ARCURAFANSUB, "arcurafansub.com", pageSize = 20, searchPageSize = 10) {
	override val listUrl = "/seri"

	override val filterCapabilities: MangaListFilterCapabilities
		get() = super.filterCapabilities.copy(
			isTagsExclusionSupported = false,
		)
}
