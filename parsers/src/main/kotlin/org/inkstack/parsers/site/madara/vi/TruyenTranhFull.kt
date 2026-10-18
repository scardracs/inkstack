package org.inkstack.parsers.site.madara.vi

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("TRUYENTRANHFULL", "Truyện Tranh Full", "vi")
internal class TruyenTranhFull(context: MangaLoaderContext) :
    MadaraParser(context, MangaParserSource.TRUYENTRANHFULL, "truyentranhfull.net", 20) {
    override val listUrl = "truyen-tranh/"
    override val tagPrefix = "the-loai/"
    override val datePattern = "dd/MM/yyyy"
}
