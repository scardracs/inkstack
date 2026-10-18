package org.inkstack.parsers.site.tr

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.uzaymanga.UzayMangaParser

@Broken
@MangaSourceParser("UZAYMANGA", "Uzay Manga", "tr")
internal class UzayManga(context: MangaLoaderContext) :
	UzayMangaParser(
		context = context,
		source = MangaParserSource.UZAYMANGA,
		domain = "uzaymanga.com",
		cdnUrl = "https://uzaymangacdn3.efsaneler.can.re",
	)
