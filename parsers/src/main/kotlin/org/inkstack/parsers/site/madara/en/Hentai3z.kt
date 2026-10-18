package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Redirect to @hentai20")
@MangaSourceParser("HENTAI3Z", "Hentai3z", "en", ContentType.HENTAI)
internal class Hentai3z(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.HENTAI3Z, "manga18h.xyz", pageSize = 20) {
	override val withoutAjax = true
}
