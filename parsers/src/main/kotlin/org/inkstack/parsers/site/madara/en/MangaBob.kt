package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken
@MangaSourceParser("MANGABOB", "MangaBob", "en")
internal class MangaBob(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANGABOB, "mangabob.com") {
	override val postReq = true
}
