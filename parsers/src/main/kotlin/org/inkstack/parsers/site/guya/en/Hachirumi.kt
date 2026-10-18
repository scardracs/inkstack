package org.inkstack.parsers.site.guya.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.guya.GuyaParser

@MangaSourceParser("HACHIRUMI", "Hachirumi", "en", ContentType.HENTAI)
internal class Hachirumi(context: MangaLoaderContext) :
	GuyaParser(context, MangaParserSource.HACHIRUMI, "hachirumi.com")
