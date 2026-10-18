package org.inkstack.parsers.site.manga18.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.manga18.Manga18Parser

@MangaSourceParser("PORNCOMIC18", "18PornComic", "en", ContentType.HENTAI)
internal class PornComic18(context: MangaLoaderContext) :
	Manga18Parser(context, MangaParserSource.PORNCOMIC18, "18porncomic.com") {
	override val selectTag = "div.item:not(.info_label) div.info_value a"
}
