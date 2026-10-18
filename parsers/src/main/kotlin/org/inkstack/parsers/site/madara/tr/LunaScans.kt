package org.inkstack.parsers.site.madara.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("LUNASCANS", "LunaScans", "tr", ContentType.HENTAI)
internal class LunaScans(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.LUNASCANS, "tuhafscans.com") {
	override val postReq = true
	override val datePattern = "dd MMMM yyyy"
}
