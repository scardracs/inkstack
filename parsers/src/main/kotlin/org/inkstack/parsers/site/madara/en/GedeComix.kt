package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("GEDECOMIX", "GedeComix", "en", ContentType.HENTAI)
internal class GedeComix(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.GEDECOMIX, "gedecomix.com", 18) {
	override val tagPrefix = "comics-tag/"
	override val listUrl = "porncomic/"
}
