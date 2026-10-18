package org.inkstack.parsers.site.fuzzydoodle.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaListFilterOptions
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.fuzzydoodle.FuzzyDoodleParser
import java.util.EnumSet

@MangaSourceParser("SCYLLACOMICS", "ScyllaComics", "en")
internal class ScyllaComics(context: MangaLoaderContext) :
	FuzzyDoodleParser(context, MangaParserSource.SCYLLACOMICS, "scyllacomics.xyz") {

	override suspend fun getFilterOptions() = MangaListFilterOptions(
		availableContentTypes = EnumSet.of(
			ContentType.MANGA,
			ContentType.MANHWA,
			ContentType.MANHUA,
		),
	)
}
