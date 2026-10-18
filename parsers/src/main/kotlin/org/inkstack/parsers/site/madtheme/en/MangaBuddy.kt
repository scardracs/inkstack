package org.inkstack.parsers.site.madtheme.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madtheme.MadthemeParser

@Broken("Website down forever")
@MangaSourceParser("MANGABUDDY", "MangaBuddy", "en")
internal class MangaBuddy(context: MangaLoaderContext) :
	MadthemeParser(context, MangaParserSource.MANGABUDDY, "mangabuddy.com")
