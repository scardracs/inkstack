package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.config.ConfigKey
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Selectors outdated (empty list) — pending smoke after ww CDN domain")
@MangaSourceParser("LUFFYMANGA", "LuffyManga", "en")
internal class LuffyManga(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.LUFFYMANGA, "luffymanga.com", 10) {
	override val configKeyDomain = ConfigKey.Domain("luffymanga.com", "ww19.luffymanga.com")
}
