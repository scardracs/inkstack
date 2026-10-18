package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("TATAKAE_SCANS", "TatakaeScans", "pt")
internal class TatakaeScansParser(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.TATAKAE_SCANS, "tatakaescan.com", pageSize = 10) {
	override val datePattern: String = "dd 'de' MMMMM 'de' yyyy"
}
