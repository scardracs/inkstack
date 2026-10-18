package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("MANGAWEEBS", "MangaWeebs", "en")
internal class MangaWeebs(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANGAWEEBS, "mangaweebs.in", pageSize = 20) {
	override val datePattern = "dd MMMM HH:mm"
}
