package org.inkstack.parsers.site.comicaso.id

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.comicaso.ComicasoParser

@MangaSourceParser("MEDUSASCANS", "Medusascans", "id", ContentType.HENTAI)
internal class Medusascans(context: MangaLoaderContext) :
	ComicasoParser(context, MangaParserSource.MEDUSASCANS, "medusascans.pro", pageSize = 16)
