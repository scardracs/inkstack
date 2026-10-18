package org.inkstack.parsers.site.zeistmanga.ar

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.zeistmanga.ZeistMangaParser
import org.inkstack.parsers.model.ContentType

@MangaSourceParser("YURIMOONSUB", "Yurimoonsub.blogspot.com", "ar", type = ContentType.HENTAI)
internal class YuriMoonSub(context: MangaLoaderContext) :
	ZeistMangaParser(context, MangaParserSource.YURIMOONSUB, "yurimoonsub.blogspot.com")
