package org.inkstack.parsers.site.mangak.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.Demographic
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangak.MangaKParser

@MangaSourceParser("TOONTOPIO", "ToonTop.io", "en", ContentType.HENTAI)
internal class ToonTopIO(context: MangaLoaderContext) :
	MangaKParser(context, MangaParserSource.TOONTOPIO, "toontop.io") {

	// The site offers the filter, but no title carries a demographic, so every value lists nothing.
	override val supportedDemographics: Set<Demographic> = emptySet()
}
