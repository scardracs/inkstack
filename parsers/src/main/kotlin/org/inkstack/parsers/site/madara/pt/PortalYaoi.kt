package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("PORTALYAOI", "PortalYaoi", "pt", ContentType.HENTAI)
internal class PortalYaoi(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.PORTALYAOI, "portalyaoi.com", 10) {
	override val tagPrefix = "genero/"
	override val datePattern: String = "dd/MM"
}
