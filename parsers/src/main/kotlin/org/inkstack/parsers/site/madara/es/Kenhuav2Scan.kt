package org.inkstack.parsers.site.madara.es

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken
@MangaSourceParser("KENHUAV2SCANK", "Kenhuav2Scan", "es")
internal class Kenhuav2Scan(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.KENHUAV2SCANK, "kenhuav2scan.com")
