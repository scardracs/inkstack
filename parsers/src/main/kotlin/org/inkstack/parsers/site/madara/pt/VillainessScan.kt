package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken
@MangaSourceParser("VILLAINESSSCAN", "VillainessScan", "pt", ContentType.HENTAI)
internal class VillainessScan(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.VILLAINESSSCAN, "villainessscan.xyz", pageSize = 10) {
	override val datePattern: String = "dd 'de' MMMM 'de' yyyy"
}
