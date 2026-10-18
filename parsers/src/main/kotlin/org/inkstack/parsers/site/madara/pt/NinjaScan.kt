package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("NINJASCAN", "NinjaComics", "pt")
internal class NinjaScan(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.NINJASCAN, "ninjacomics.xyz") {
	override val datePattern: String = "dd 'de' MMMMM 'de' yyyy"
}
