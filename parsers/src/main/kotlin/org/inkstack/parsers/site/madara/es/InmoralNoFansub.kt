package org.inkstack.parsers.site.madara.es

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser
import org.inkstack.parsers.Broken

@Broken
@MangaSourceParser("INMORALNOFANSUB", "InmoralNoFansub", "es")
internal class InmoralNoFansub(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.INMORALNOFANSUB, "inmoralnofansub.xyz") {
	override val datePattern = "dd/MM/yyyy"
}
