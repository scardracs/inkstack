package org.inkstack.parsers.site.keyoapp.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.keyoapp.KeyoappParser

@Broken
@MangaSourceParser("LAIDBACKSCANS", "LaidBackScans", "en")
internal class LaidBackScans(context: MangaLoaderContext) :
	KeyoappParser(context, MangaParserSource.LAIDBACKSCANS, "laidbackscans.org")
