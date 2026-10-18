package org.inkstack.parsers.site.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.uzaymanga.UzayMangaParser

@MangaSourceParser("TENSHIMANGA", "Tenshi Manga", "tr")
internal class TenshiManga(context: MangaLoaderContext) :
	UzayMangaParser(
		context = context,
		source = MangaParserSource.TENSHIMANGA,
		domain = "tenshimanga.com",
		cdnUrl = "https://tenshimangacdn4.efsaneler.can.re",
	)
