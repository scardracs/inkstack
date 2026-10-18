package org.inkstack.parsers.site.madara.es

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken
@MangaSourceParser("LKSCANLATION", "LkScanlation", "es")
internal class LkScanlation(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.LKSCANLATION, "lkscanlation.com") {
	override val tagPrefix = "manhwa-genre/"
	override val listUrl = "manhwa/"
}
