package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("KUMASCANS", "Retsu", "en")
internal class KumaScans(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.KUMASCANS, "retsu.org") {
	override val tagPrefix = "genre/"
}
