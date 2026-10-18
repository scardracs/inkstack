package org.inkstack.parsers.site.madara.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.initmanga.InitMangaParser

@MangaSourceParser("RAGNARSCANS", "Ragnarscans", "tr")
internal class Ragnarscans(context: MangaLoaderContext) :
	InitMangaParser(
		context = context,
		source = MangaParserSource.RAGNARSCANS,
		domain = "ragnarscans.com",
		pageSize = 20,
		searchPageSize = 20,
		mangaUrlDirectory = "manga",
		popularUrlSlug = "en-cok-takip-edilenler",
		isCloudflareProtected = true,
	)
