package org.inkstack.parsers.site.heancms.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.heancms.HeanCms

@Broken("Not dead, changed template")
@MangaSourceParser("TEMPLESCAN", "TempleScan", "en")
internal class TempleScan(context: MangaLoaderContext) :
	HeanCms(context, MangaParserSource.TEMPLESCAN, "templetoons.com") {
	override val pathManga = "comic"

	override val apiPath: String
		get() = "$domain/api"
}
