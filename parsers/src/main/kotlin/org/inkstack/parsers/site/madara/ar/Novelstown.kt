package org.inkstack.parsers.site.madara.ar

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Site unreachable (DNS)")
@MangaSourceParser("NOVELSTOWN", "NovelsTown", "ar")
internal class Novelstown(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.NOVELSTOWN, "novelstown.com") {
	override val datePattern = "d MMMM، yyyy"
}
