package org.inkstack.parsers.site.madara.es

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Site unreachable (DNS)")
@MangaSourceParser("RIGHTDARKSCAN", "RightDarkScan", "es")
internal class RightdarkScan(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.RIGHTDARKSCAN, "rsdleft.com", 10)
