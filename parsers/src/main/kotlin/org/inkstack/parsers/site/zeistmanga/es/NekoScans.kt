package org.inkstack.parsers.site.zeistmanga.es

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.zeistmanga.ZeistMangaParser
import org.inkstack.parsers.Broken

@Broken("Site unreachable (timeout)")
@MangaSourceParser("NEKOSCANS", "NekoScans", "es")
internal class NekoScans(context: MangaLoaderContext) :
	ZeistMangaParser(context, MangaParserSource.NEKOSCANS, "www.nekoscans.org")
