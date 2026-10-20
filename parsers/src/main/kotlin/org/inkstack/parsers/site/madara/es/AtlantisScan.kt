package org.inkstack.parsers.site.madara.es

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.config.ConfigKey
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Selectors outdated (empty list) — pending smoke after ww1 domain")
@MangaSourceParser("ATLANTISSCAN", "AtlantisScan", "es")
internal class AtlantisScan(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.ATLANTISSCAN, "scansatlanticos.com", pageSize = 50) {
	override val configKeyDomain = ConfigKey.Domain("scansatlanticos.com", "ww1.scansatlanticos.com")
	override val datePattern = "dd/MM/yyyy"
}
