package org.inkstack.parsers.site.onemanga.fr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.onemanga.OneMangaParser

@MangaSourceParser("MYHEROACADEMIASCAN", "MyHeroacAdemiaScan", "fr")
internal class MyHeroacAdemiaScan(context: MangaLoaderContext) :
	OneMangaParser(context, MangaParserSource.MYHEROACADEMIASCAN, "myheroacademiascan.com")
