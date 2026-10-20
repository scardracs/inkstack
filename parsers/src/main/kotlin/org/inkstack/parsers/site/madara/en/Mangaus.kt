package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Selectors outdated (empty list)")
@MangaSourceParser("MANGAUS", "Mangaus", "en")
internal class Mangaus(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANGAUS, "mangaus.xyz") {
	override val withoutAjax = true
}
