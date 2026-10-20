package org.inkstack.parsers.site.cupfox.fr

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.cupfox.CupFoxParser

@Broken("Site unreachable (connection refused)")
@MangaSourceParser("FRMANGA", "FrManga", "fr")
internal class FrManga(context: MangaLoaderContext) :
	CupFoxParser(context, MangaParserSource.FRMANGA, "www.frmanga.com")
