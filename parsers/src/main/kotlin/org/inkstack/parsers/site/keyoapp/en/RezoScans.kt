package org.inkstack.parsers.site.keyoapp.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.keyoapp.KeyoappParser

@MangaSourceParser("REZOSCANS", "RezoScans", "en")
internal class RezoScans(context: MangaLoaderContext) :
	KeyoappParser(context, MangaParserSource.REZOSCANS, "rezoscans.com")
