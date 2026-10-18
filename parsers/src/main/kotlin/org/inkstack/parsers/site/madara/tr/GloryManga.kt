package org.inkstack.parsers.site.madara.tr

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Redirect to @MANGAGEZGINI")
@MangaSourceParser("GLORYMANGA", "GloryManga", "tr")
internal class GloryManga(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.GLORYMANGA, "mangagezgini.site", 18) {
	override val datePattern = "dd/MM/yyyy"
}
