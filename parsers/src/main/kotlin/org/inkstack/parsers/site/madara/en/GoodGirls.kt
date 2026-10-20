package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Selectors outdated (empty list)")
@MangaSourceParser("GOODGIRLS", "GoodGirls", "en")
internal class GoodGirls(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.GOODGIRLS, "goodgirls.moe", 10) {
	override val selectDesc = "div.post-content_item:contains(Synopsis) div.summary-content"
}
