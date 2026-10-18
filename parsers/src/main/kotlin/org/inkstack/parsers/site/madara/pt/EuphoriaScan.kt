package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("EUPHORIASCAN", "EuphoriaScan", "pt", ContentType.HENTAI)
internal class EuphoriaScan(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.EUPHORIASCAN, "euphoriascan.com", 10) {
	override val datePattern: String = "dd 'de' MMMM 'de' yyyy"
}
