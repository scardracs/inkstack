package org.inkstack.parsers.site.guya.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.guya.GuyaParser

@MangaSourceParser("DANKE", "DankeFursLesen", "en")
internal class Danke(context: MangaLoaderContext) :
	GuyaParser(context, MangaParserSource.DANKE, "danke.moe")
