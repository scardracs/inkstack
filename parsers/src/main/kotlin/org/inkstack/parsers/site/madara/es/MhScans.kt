package org.inkstack.parsers.site.madara.es

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("MHSCANS", "MhScans", "es")
internal class MhScans(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MHSCANS, "mhscans.mundoalterno.org") {
	override val datePattern = "d 'de' MMMMM 'de' yyyy"
	override val listUrl = "series/"
}
