package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

// redirect to @MangaZin
@MangaSourceParser("MANGABEE", "MangaBee", "en", ContentType.HENTAI)
internal class MangaBee(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANGABEE, "mangazin.org") {
	override val datePattern = "MM/dd/yyyy"
}
