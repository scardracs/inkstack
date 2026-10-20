package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Site unreachable (connection error)")
@MangaSourceParser("DARKSCAN", "Dark-Scan", "en")
internal class DarkScan(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.DARKSCAN, "dark-scan.com")
