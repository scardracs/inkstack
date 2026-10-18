package org.inkstack.parsers.site.madara.fr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("PANTHEONSCAN", "PantheonScan.com", "fr")
internal class PantheonScan(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.PANTHEONSCAN, "pantheon-scan.com") {
	override val datePattern = "d MMMM yyyy"
}
