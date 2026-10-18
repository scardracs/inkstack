package org.inkstack.parsers.site.mmrcms.fr

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mmrcms.MmrcmsParser
import java.util.*

@Broken
@MangaSourceParser("JPMANGAS", "JpMangas", "fr")
internal class JpMangas(context: MangaLoaderContext) :
	MmrcmsParser(context, MangaParserSource.JPMANGAS, "jpmangas.xyz") {
	override val sourceLocale: Locale = Locale.ENGLISH
}
