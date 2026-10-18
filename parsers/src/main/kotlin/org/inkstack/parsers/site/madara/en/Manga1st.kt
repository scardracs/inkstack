package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("MANGA1ST", "Manga1st", "en")
internal class Manga1st(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANGA1ST, "manga1st.online") {
	override val datePattern = "d MMMM، yyyy"
}
