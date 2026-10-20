package org.inkstack.parsers.site.madara.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser
import org.inkstack.parsers.Broken

@Broken("Site unreachable (timeout)")
@MangaSourceParser("STRAYFANSUB", "StrayFansub", "tr")
internal class StrayFansub(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.STRAYFANSUB, "strayfansub.com", 16) {
	override val tagPrefix = "seri-turu/"
}
