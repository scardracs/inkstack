package org.inkstack.parsers.site.zeistmanga.id

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.zeistmanga.ZeistMangaParser
import org.inkstack.parsers.model.MangaChapter
import org.inkstack.parsers.model.MangaPage
import org.inkstack.parsers.util.toAbsoluteUrl
import org.inkstack.parsers.util.generateUid
import org.inkstack.parsers.util.parseHtml
import org.inkstack.parsers.util.selectFirstOrThrow

@MangaSourceParser("ULASCOMIC", "UlasComic", "id")
internal class UlasComic(context: MangaLoaderContext):
	ZeistMangaParser(context, MangaParserSource.ULASCOMIC, "www.ulascomic01.xyz") {
	
	override suspend fun getPages(chapter: MangaChapter): List<MangaPage> {
		val doc = webClient.httpGet(chapter.url.toAbsoluteUrl(domain)).parseHtml()
		return doc.selectFirstOrThrow("script:containsData(config['chapterImage'])")
			.data()
			.substringAfter("config['chapterImage'] = [")
			.substringBefore("];")
			.split("\",")
			.map { url ->
				val cleanUrl = url.trim().replace("\"", "")
				MangaPage(
					id = generateUid(cleanUrl),
					url = cleanUrl,
					preview = null,
					source = source,
				)
			}
	}
}
