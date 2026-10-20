package org.inkstack.parsers.site.zmanga.id

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.zmanga.ZMangaParser

@Broken("Site unreachable (DNS)")
@MangaSourceParser("YURAMANGA", "YuraManga", "id")
internal class YuraManga(context: MangaLoaderContext) :
	ZMangaParser(context, MangaParserSource.YURAMANGA, "www.yuramanga.my.id")

