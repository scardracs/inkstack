package org.inkstack.parsers.site.ru.multichan

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.config.ConfigKey
import org.inkstack.parsers.model.MangaParserSource

@MangaSourceParser("MANGACHAN", "Манга-тян", "ru")
internal class MangaChanParser(context: MangaLoaderContext) : ChanParser(context, MangaParserSource.MANGACHAN) {
	override val configKeyDomain = ConfigKey.Domain("manga-chan.me")
}
