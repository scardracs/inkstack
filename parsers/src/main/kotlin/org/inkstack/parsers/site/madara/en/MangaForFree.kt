package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("MANGAFORFREE", "MangaForFree", "en", ContentType.HENTAI)
internal class MangaForFree(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANGAFORFREE, "mangaforfree.com", 10) {
	override val postReq = true
}
