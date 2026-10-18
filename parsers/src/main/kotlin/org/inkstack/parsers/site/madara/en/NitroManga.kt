package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("NITROMANGA", "NitroManga", "en")
internal class NitroManga(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.NITROMANGA, "nitroscans.net", pageSize = 18)
