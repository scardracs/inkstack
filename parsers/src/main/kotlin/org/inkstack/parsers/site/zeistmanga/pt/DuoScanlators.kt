package org.inkstack.parsers.site.zeistmanga.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.zeistmanga.ZeistMangaParser

@MangaSourceParser("DUOSCANLATORS", "DuoScanlators", "pt")
internal class DuoScanlators(context: MangaLoaderContext) :
	ZeistMangaParser(context, MangaParserSource.DUOSCANLATORS, "duoscanlators.blogspot.com")
