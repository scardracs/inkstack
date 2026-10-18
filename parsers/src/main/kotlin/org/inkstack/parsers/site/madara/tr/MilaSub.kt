package org.inkstack.parsers.site.madara.tr

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Content not found or removed")
@MangaSourceParser("MILASUB", "MilaSub", "tr")
internal class MilaSub(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MILASUB, "milascans.tr", 20) {
	override val datePattern = "d MMMM yyyy"
}
