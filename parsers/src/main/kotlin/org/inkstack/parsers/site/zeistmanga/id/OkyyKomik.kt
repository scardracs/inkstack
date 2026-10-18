package org.inkstack.parsers.site.zeistmanga.id

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.zeistmanga.ZeistMangaParser

@MangaSourceParser("OKYYKOMIK", "OkyyKomik", "id")
internal class OkyyKomik(context: MangaLoaderContext) :
	ZeistMangaParser(context, MangaParserSource.OKYYKOMIK, "www.okyykomik.my.id")
