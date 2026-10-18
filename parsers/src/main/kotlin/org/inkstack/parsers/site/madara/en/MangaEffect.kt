package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Redirect to @MANGAREAD")
@MangaSourceParser("MANGAEFFECT", "MangaEffect", "en")
internal class MangaEffect(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANGAEFFECT, "www.mangaread.org") {
	override val datePattern = "dd.MM.yyyy"
	override val withoutAjax = true
}
