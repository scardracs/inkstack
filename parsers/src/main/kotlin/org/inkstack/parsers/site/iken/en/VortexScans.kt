package org.inkstack.parsers.site.iken.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.iken.IkenParser

@MangaSourceParser("VORTEXSCANS", "VortexScans", "en")
internal class VortexScans(context: MangaLoaderContext) :
    IkenParser(context, MangaParserSource.VORTEXSCANS, "vortexscans.org", 18, true)
