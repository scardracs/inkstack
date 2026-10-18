package org.inkstack.parsers.site.zeistmanga.ar

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.zeistmanga.ZeistMangaParser

@MangaSourceParser("XSANOMANGA", "XsanoManga", "ar")
internal class XsanoManga(context: MangaLoaderContext) :
	ZeistMangaParser(context, MangaParserSource.XSANOMANGA, "www.xsano-manga.com")
