package org.inkstack.parsers.site.keyoapp.ar

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.keyoapp.KeyoappParser

@MangaSourceParser("SCANS4U", "4uScans", "ar")
internal class Scans4u(context: MangaLoaderContext) :
	KeyoappParser(context, MangaParserSource.SCANS4U, "4uscans.com")
