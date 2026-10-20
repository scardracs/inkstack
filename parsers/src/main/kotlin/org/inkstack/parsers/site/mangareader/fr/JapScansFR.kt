package org.inkstack.parsers.site.mangareader.fr

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser
import java.util.Locale

@Broken("Site unreachable (connection error)")
@MangaSourceParser("JAPSCANSFR", "JapScans.fr", "fr")
internal class JapScansFR(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.JAPSCANSFR, "japscans.fr", pageSize = 20, searchPageSize = 10) {
	override val sourceLocale: Locale = Locale.ENGLISH
}
