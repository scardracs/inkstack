package org.inkstack.parsers.site.madara.es

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Selectors outdated (empty list)")
@MangaSourceParser("HOUSEOFOTAKUS", "HouseOfOtakus", "es")
internal class HouseOfOtakus(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.HOUSEOFOTAKUS, "houseofotakus.xyz")
