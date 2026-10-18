package org.inkstack.parsers.site.mangareader.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@MangaSourceParser("SNOWMACHINETRANSLATION", "Snow Machine Translation", "en")

internal class SnowMachineTranslation(context: MangaLoaderContext) :
    MangaReaderParser(context, MangaParserSource.SNOWMACHINETRANSLATION, "snowmachinetranslation.com", pageSize = 24, searchPageSize = 10) {
    override val listUrl = "/manga"


}
