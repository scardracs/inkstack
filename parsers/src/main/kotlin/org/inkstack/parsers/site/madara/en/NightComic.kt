package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Selectors outdated (ajax 404)")
@MangaSourceParser("NIGHTCOMIC", "Night Comic", "en")
internal class NightComic(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.NIGHTCOMIC, "www.nightcomic.com") {
	override val postReq = true
}
