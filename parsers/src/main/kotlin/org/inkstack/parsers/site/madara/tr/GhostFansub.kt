package org.inkstack.parsers.site.madara.tr

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Redirect to @GRIMELEK")
@MangaSourceParser("GHOSTFANSUB", "GhostFansub", "tr")
internal class GhostFansub(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.GHOSTFANSUB, "ghostfansub.co", 18)
// you now need to log in to access content
