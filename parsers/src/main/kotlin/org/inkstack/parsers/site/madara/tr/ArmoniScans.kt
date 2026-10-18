package org.inkstack.parsers.site.madara.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("ARMONISCANS", "ArmoniScans", "tr")
internal class ArmoniScans(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.ARMONISCANS, "armoniscans.net") {
	override val tagPrefix = "tur/"
}
