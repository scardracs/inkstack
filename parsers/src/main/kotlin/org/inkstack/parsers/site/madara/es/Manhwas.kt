package org.inkstack.parsers.site.madara.es

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("MANHWAS_ES", "Manhwas.es", "es", ContentType.HENTAI)
internal class Manhwas(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANHWAS_ES, "manhwas.es", 30)
