package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken
@MangaSourceParser("MANHWA18APP", "Manhwa18.app", "en", ContentType.HENTAI)
internal class Manhwa18App(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANHWA18APP, "manhwa18.app") {
	override val postReq = true
}
