package org.inkstack.parsers.site.zmanga.id

import org.jsoup.nodes.Document
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaChapter
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.zmanga.ZMangaParser
import org.inkstack.parsers.util.attrAsRelativeUrl
import org.inkstack.parsers.util.generateUid
import org.inkstack.parsers.util.mapChapters
import org.inkstack.parsers.util.selectFirstOrThrow
import java.text.SimpleDateFormat

@MangaSourceParser("SHIRO_DOUJIN", "ShiroDoujin", "id", ContentType.HENTAI)
internal class ShiroDoujin(context: MangaLoaderContext) :
	ZMangaParser(context, MangaParserSource.SHIRO_DOUJIN, "shirodoujin.com") {

	override suspend fun getChapters(doc: Document): List<MangaChapter> {
		val dateFormat = SimpleDateFormat(datePattern, sourceLocale)
		return doc.body().select(selectChapter).mapChapters(reversed = true) { i, li ->
			val a = li.selectFirstOrThrow("a")
			val href = a.attrAsRelativeUrl("href")
			val dateText = li.selectFirst(selectDate)?.text()
			val numChapter = li.selectFirstOrThrow(".flexch-infoz span").html().substringAfterLast("Chapter ")
				.substringBefore("<span")
			MangaChapter(
				id = generateUid(href),
				title = null,
				number = i + 1f,
				volume = 0,
				url = href,
				uploadDate = parseChapterDate(
					dateFormat,
					dateText,
				),
				source = source,
				scanlator = null,
				branch = null,
			)
		}
	}
}
