package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("SLEEPYTRANSLATIONS", "Sleepy Translations", "en")
internal class SleepyTranslations(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.SLEEPYTRANSLATIONS, "sleepytranslations.com", 16) {
	override val listUrl = "series/"
	override val tagPrefix = "genre/"
}
