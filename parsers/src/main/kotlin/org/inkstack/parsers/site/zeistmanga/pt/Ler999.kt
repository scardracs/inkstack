package org.inkstack.parsers.site.zeistmanga.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.zeistmanga.ZeistMangaParser

@MangaSourceParser("LER999", "Ler999", "pt")
internal class Ler999(context: MangaLoaderContext) :
	ZeistMangaParser(context, MangaParserSource.LER999, "ler999.blogspot.com")
