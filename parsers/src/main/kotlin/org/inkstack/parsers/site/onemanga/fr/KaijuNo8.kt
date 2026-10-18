package org.inkstack.parsers.site.onemanga.fr

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.onemanga.OneMangaParser

@Broken
@MangaSourceParser("KAIJUNO8", "KaijuNo8", "fr")
internal class KaijuNo8(context: MangaLoaderContext) :
	OneMangaParser(context, MangaParserSource.KAIJUNO8, "kaijuno8.fr")
