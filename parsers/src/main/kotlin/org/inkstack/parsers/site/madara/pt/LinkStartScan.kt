package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken
@MangaSourceParser("LINKSTARTSCAN", "LinkStartScan", "pt")
internal class LinkStartScan(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.LINKSTARTSCAN, "www.linkstartscan.xyz") {
	override val datePattern: String = "dd/MM/yyyy"
}
