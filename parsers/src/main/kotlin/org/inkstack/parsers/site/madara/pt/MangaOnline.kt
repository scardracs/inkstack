package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("MANGAONLINE_BLOG", "MangaOnline", "pt")
internal class MangaOnline(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANGAONLINE_BLOG, "mangaonline.blog", 16) {
	override val datePattern: String = "dd 'de' MMMMM 'de' yyyy"
}
