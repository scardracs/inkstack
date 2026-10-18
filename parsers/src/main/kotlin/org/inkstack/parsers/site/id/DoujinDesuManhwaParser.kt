package org.inkstack.parsers.site.id

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import java.util.*

@MangaSourceParser("DOUJINDESU_MANHWA", "DoujinDesu Manhwa", "id")
internal class DoujinDesuManhwaParser(context: MangaLoaderContext) :
	BaseDoujinDesuParser(context, MangaParserSource.DOUJINDESU_MANHWA) {

	override val defaultTypes: String = "manhwa"

	override val availableContentTypes: Set<ContentType> = EnumSet.of(ContentType.MANHWA)
}
