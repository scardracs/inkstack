package org.inkstack.parsers.site.scan.pt

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.scan.ScanParser

@Broken("Site unreachable (timeout)")
@MangaSourceParser("MANGABR", "MangaBr", "pt")
internal class MangaBr(context: MangaLoaderContext) :
	ScanParser(context, MangaParserSource.MANGABR, "mangabr.net")
