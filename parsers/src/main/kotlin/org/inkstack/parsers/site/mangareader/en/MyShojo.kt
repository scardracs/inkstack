package org.inkstack.parsers.site.mangareader.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaListFilterCapabilities
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@MangaSourceParser("MYSHOJO", "MyShojo", "en")
internal class MyShojo(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.MYSHOJO, "myshojo.com", pageSize = 20, searchPageSize = 10) {
	override val filterCapabilities: MangaListFilterCapabilities
		get() = super.filterCapabilities.copy(
			isTagsExclusionSupported = false,
		)
}
