package org.inkstack.parsers.site.madara.id

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser
import java.util.Locale

@MangaSourceParser("XMANHWA", "XManhwa", "id", ContentType.HENTAI)
internal class XManhwa(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.XMANHWA, "www.manhwaden.com", 10) {
	override val sourceLocale: Locale = Locale.ENGLISH
	override val selectPage = "img"
}
