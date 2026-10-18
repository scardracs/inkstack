package org.inkstack.parsers.site.mangabox.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.config.ConfigKey
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangabox.MangaboxParser
@Broken
@MangaSourceParser("MANGANELO_COM", "MangaNelo.com", "en")
internal class MangaNeloCom(context: MangaLoaderContext) :
	MangaboxParser(context, MangaParserSource.MANGANELO_COM) {
	override val configKeyDomain = ConfigKey.Domain("nelomanga.com", "m.manganelo.com", "chapmanganelo.com")
	override val otherDomain = "chapmanganelo.com"
}
