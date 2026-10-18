package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("DREAMSCAN", "DreamScan", "pt")
internal class DreamScan(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.DREAMSCAN, "fairydream.com.br")
