package org.inkstack.parsers.site.madtheme.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madtheme.MadthemeParser

@Broken("Redirects to ToonTop")
@MangaSourceParser("TOONILY_ME", "Toonily.Me", "en", ContentType.HENTAI)
internal class ToonilyMe(context: MangaLoaderContext) :
	MadthemeParser(context, MangaParserSource.TOONILY_ME, "toonily.me") {
	override val selectDesc = "div.summary div.section-body p.content"
}
