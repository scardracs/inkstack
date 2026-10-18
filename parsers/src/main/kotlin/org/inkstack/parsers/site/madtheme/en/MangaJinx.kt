package org.inkstack.parsers.site.madtheme.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.config.ConfigKey
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madtheme.MadthemeParser

@Broken("mgjinx.vip still broken in-app; needs further parser work")
@MangaSourceParser("MANGAJINX", "MangaJinx", "en")
internal class MangaJinx(context: MangaLoaderContext) :
	MadthemeParser(context, MangaParserSource.MANGAJINX, "mgjinx.vip") {

	override val configKeyDomain = ConfigKey.Domain("mgjinx.vip", "mgjinx.com")

	override val listUrl = "search"
}
