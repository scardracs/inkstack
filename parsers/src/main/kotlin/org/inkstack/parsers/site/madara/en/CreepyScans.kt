package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Site unreachable (connection error)")
@MangaSourceParser("CREEPYSCANS", "CreepyScans", "en")
internal class CreepyScans(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.CREEPYSCANS, "creepyscans.com") {
	override val stylePage = ""
}
