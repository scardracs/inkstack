package org.inkstack.parsers.site.madara.es

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("SAPPHIRESCAN", "SapphireScan", "es")
internal class SapphireScan(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.SAPPHIRESCAN, "sapphirescan.com")
