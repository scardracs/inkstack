package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("MANGAYY", "MangaYY", "en")
internal class MangaYY(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANGAYY, "mangayy.org") {

	override val datePattern = "dd MMMM, yyyy"
}
