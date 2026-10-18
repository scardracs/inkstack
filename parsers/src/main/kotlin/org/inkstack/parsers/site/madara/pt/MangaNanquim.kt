package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken
@MangaSourceParser("MANGANANQUIM", "MangaNanquim", "pt")
internal class MangaNanquim(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANGANANQUIM, "mangananquim.site", 10) {
	override val datePattern: String = "d 'de' MMMM 'de' yyyy"
	override val listUrl = "ler-manga/"
	override val tagPrefix = "manga-genero/"
}
