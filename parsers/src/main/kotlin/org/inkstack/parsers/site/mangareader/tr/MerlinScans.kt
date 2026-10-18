package org.inkstack.parsers.site.mangareader.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.initmanga.InitMangaParser

@MangaSourceParser("MERLINSCANS", "MerlinScans", "tr")
internal class MerlinScans(context: MangaLoaderContext) :
	InitMangaParser(
		context = context,
		source = MangaParserSource.MERLINSCANS,
		domain = "merlintoon.com",
		pageSize = 20,
		searchPageSize = 20,
		latestUrlSlug = "son-guncellenenler",
	)
