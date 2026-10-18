package org.inkstack.parsers.site.nepnep.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.nepnep.NepnepParser

// site closed in favour of weeb central
@Broken
@MangaSourceParser("MANGASEE", "MangaSee", "en")
internal class MangaSee(context: MangaLoaderContext) :
	NepnepParser(context, MangaParserSource.MANGASEE, "mangasee123.com")
