package org.inkstack.parsers.site.mangareader.ar

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaListFilterCapabilities
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@Broken("Selectors outdated (empty list)")
@MangaSourceParser("SCARMANGA", "ScarManga", "ar")
internal class ScarManga(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.SCARMANGA, "scarmanga.com", pageSize = 20, searchPageSize = 10) {
	override val listUrl = "/series"
	override val filterCapabilities: MangaListFilterCapabilities
		get() = super.filterCapabilities.copy(
			isTagsExclusionSupported = false,
		)
}
