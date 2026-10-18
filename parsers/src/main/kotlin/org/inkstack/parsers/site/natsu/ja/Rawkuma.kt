package org.inkstack.parsers.site.natsu.ja

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.config.ConfigKey
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.natsu.NatsuParser

@MangaSourceParser("RAWKUMA", "Rawkuma", "ja")
internal class Rawkuma(context: MangaLoaderContext) :
	NatsuParser(context, MangaParserSource.RAWKUMA, pageSize = 24) {

	override val configKeyDomain = ConfigKey.Domain("rawkuma.net")
}
