package org.inkstack.parsers.site.mangareader.es

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaListFilterCapabilities
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser
import java.util.*

@Broken("Site unavailable (HTTP 503)")
@MangaSourceParser("TENKAISCAN", "TenkaiScan", "es", ContentType.HENTAI)
internal class TenkaiScan(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.TENKAISCAN, "falcoscan.net", 20, 10) {
	override val sourceLocale: Locale = Locale.ENGLISH
	override val filterCapabilities: MangaListFilterCapabilities
		get() = super.filterCapabilities.copy(
			isTagsExclusionSupported = false,
		)
}
