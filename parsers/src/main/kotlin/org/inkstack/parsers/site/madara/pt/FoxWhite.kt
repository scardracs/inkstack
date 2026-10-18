package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken
@MangaSourceParser("FOXWHITE", "FoxWhite", "pt")
internal class FoxWhite(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.FOXWHITE, "foxwhite.com.br")
