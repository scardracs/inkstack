package org.inkstack.parsers.site.madara.es

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Site unreachable (DNS)")
@MangaSourceParser("RAGNAROKSCAN", "RagnarokScan", "es")
internal class RagnarokScan(context: MangaLoaderContext) :
    MadaraParser(context, MangaParserSource.RAGNAROKSCAN, "ragnarokscan.com") {
    override val stylePage = ""
    override val listUrl = "series/"
    override val tagPrefix = "genero/"
}
