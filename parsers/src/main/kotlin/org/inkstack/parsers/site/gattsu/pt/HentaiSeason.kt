package org.inkstack.parsers.site.gattsu.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.gattsu.GattsuParser

@MangaSourceParser("HENTAISEASON", "HentaiSeason", "pt", ContentType.HENTAI)
internal class HentaiSeason(context: MangaLoaderContext) :
	GattsuParser(context, MangaParserSource.HENTAISEASON, "hentaiseason.com")
