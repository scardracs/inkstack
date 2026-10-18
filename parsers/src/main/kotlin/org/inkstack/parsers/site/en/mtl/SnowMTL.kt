package org.inkstack.parsers.site.en.mtl

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.model.ContentType

@Broken
@MangaSourceParser("SNOWMTL", "SnowMTL", "en", type = ContentType.OTHER)
internal class SnowMTL(context: MangaLoaderContext):
    MTLParser(context, source = MangaParserSource.SNOWMTL, "snowmtl.ru")
