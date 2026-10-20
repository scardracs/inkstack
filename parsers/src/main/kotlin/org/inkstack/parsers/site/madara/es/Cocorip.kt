package org.inkstack.parsers.site.madara.es

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser
import org.inkstack.parsers.Broken

@Broken("Selectors outdated (empty list)")
@MangaSourceParser("COCORIP", "Cocorip", "es")
internal class Cocorip(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.COCORIP, "cocorip.net", 16) {
	override val datePattern = "dd/MM/yyyy"
}
