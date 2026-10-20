package org.inkstack.parsers.site.madara.es

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser
import org.inkstack.parsers.Broken

@Broken("Selectors outdated (empty list)")
@MangaSourceParser("HAREMSCANN", "HaremScann", "es")
internal class HaremScann(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.HAREMSCANN, "haremscann.es") {
	override val postReq = true
}
