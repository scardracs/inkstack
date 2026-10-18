package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("YAOIX3", "3XYaoi", "pt", ContentType.HENTAI)
internal class YaoiX3(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.YAOIX3, "3xyaoi.com") {
	override val datePattern = "dd/MM/yyyy"
	override val listUrl = "bl/"
	override val tagPrefix = "genero/"
}
