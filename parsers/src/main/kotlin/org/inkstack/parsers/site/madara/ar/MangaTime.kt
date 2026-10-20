package org.inkstack.parsers.site.madara.ar

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Protected by Cloudflare")
@MangaSourceParser("MANGATIME", "MangaTime", "ar")
internal class MangaTime(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANGATIME, "mangatime.org") {
	override val datePattern = "d MMMM، yyyy"
}
