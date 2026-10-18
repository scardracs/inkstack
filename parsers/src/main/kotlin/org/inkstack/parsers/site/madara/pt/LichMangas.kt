package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("LICHMANGAS", "LichMangas", "pt")
internal class LichMangas(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.LICHMANGAS, "lichmangas.com", 10) {
	override val datePattern = "dd/MM/yyyy"
}
