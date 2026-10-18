package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("CVNSCAN", "CvnScan", "pt", ContentType.HENTAI)
internal class CvnScan(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.CVNSCAN, "covendasbruxonas.com")
