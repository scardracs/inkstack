package org.inkstack.parsers.site.scan.fr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.scan.ScanParser

@MangaSourceParser("SCANVFORG", "ScanVf.org", "fr")
internal class ScanVfOrg(context: MangaLoaderContext) :
	ScanParser(context, MangaParserSource.SCANVFORG, "scanvf.org")
