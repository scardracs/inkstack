package org.inkstack.parsers.site.zeistmanga.id

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.zeistmanga.ZeistMangaParser
import org.inkstack.parsers.Broken

@Broken("Original site closed")
@MangaSourceParser("SOBATMANKU", "Sobatmanku", "id")
internal class Sobatmanku(context: MangaLoaderContext) :
	ZeistMangaParser(context, MangaParserSource.SOBATMANKU, "www.sobatmanku19.cab")
