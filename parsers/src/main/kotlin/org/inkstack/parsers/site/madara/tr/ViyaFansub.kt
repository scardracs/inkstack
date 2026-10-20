package org.inkstack.parsers.site.madara.tr

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

//Manga +18 require login.
@Broken("Site unreachable (DNS)")
@MangaSourceParser("VIYAFANSUB", "ViyaFansub", "tr", ContentType.HENTAI)
internal class ViyaFansub(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.VIYAFANSUB, "viyafansub.com")