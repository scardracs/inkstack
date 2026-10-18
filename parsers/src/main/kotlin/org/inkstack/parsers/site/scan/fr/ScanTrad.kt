package org.inkstack.parsers.site.scan.fr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.scan.ScanParser

@MangaSourceParser("SCANTRAD", "ScanTrad", "fr")
internal class ScanTrad(context: MangaLoaderContext) :
	ScanParser(context, MangaParserSource.SCANTRAD, "scan-trad.com")
