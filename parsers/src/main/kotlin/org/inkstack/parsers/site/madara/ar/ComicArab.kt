package org.inkstack.parsers.site.madara.ar

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser
import org.inkstack.parsers.Broken

@Broken("Original site closed")
@MangaSourceParser("COMICARAB", "ComicArab", "ar")
internal class ComicArab(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.COMICARAB, "comicarab.com", pageSize = 24) {
	override val datePattern = "d MMMM، yyyy"
}
