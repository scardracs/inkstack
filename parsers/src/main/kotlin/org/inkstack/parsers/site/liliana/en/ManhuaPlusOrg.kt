package org.inkstack.parsers.site.liliana.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.liliana.LilianaParser

@MangaSourceParser("MANHUAPLUSORG", "ManhuaPlus.org", "en")
internal class ManhuaPlusOrg(context: MangaLoaderContext) :
	LilianaParser(context, MangaParserSource.MANHUAPLUSORG, "manhuaplus.org")
