package org.inkstack.parsers.site.mangareader.id

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaListFilterCapabilities
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@Broken("List paths 404 (/manga and /series); site redesign")
@MangaSourceParser("KOMIKAV", "KomikAv", "id")
internal class KomikAvParser(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.KOMIKAV, "komikav.net", pageSize = 20, searchPageSize = 10) {

	override val filterCapabilities: MangaListFilterCapabilities
		get() = super.filterCapabilities.copy(
			isTagsExclusionSupported = false,
		)
}
