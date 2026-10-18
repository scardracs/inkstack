package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("YURILIVE", "YuriLive", "pt", ContentType.HENTAI)
internal class YuriLive(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.YURILIVE, "yuri.live") {
	override val tagPrefix = "manga-genero/"
	override val datePattern: String = "dd/MM/yyyy"
}
