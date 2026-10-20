package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser
import java.util.*

@Broken("Site unreachable (connection error)")
@MangaSourceParser("GEKKOUSCANS", "GekkouScans", "pt", ContentType.HENTAI)
internal class GekkouScans(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.GEKKOUSCANS, "gekkou.site") {
	override val sourceLocale: Locale = Locale.ENGLISH
}
