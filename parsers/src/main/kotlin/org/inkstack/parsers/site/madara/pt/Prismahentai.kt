package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Site unreachable (connection error)")
@MangaSourceParser("PRISMA_HENTAI", "PrismaHentai", "pt", ContentType.HENTAI)
internal class Prismahentai(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.PRISMA_HENTAI, "prismahentai.com", 18)
