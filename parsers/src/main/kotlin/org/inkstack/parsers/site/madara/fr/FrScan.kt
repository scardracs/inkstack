package org.inkstack.parsers.site.madara.fr

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaChapter
import org.inkstack.parsers.model.MangaPage
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser
import org.inkstack.parsers.util.*

@Broken
@MangaSourceParser("FRSCAN", "Fr-Scan", "fr")
internal class FrScan(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.FRSCAN, "fr-scan.com") {
	override val stylePage = ""

	override suspend fun getPages(chapter: MangaChapter): List<MangaPage> {
		val fullUrl = chapter.url.toAbsoluteUrl(domain)
		val doc = webClient.httpGet(fullUrl).parseHtml()
		val preloadImg = doc.getElementById("chapter_preloaded_images")
		if (preloadImg != null) {
			val img = preloadImg.data().substringAfter("[").substringBeforeLast("]")
				.replace("\\", "").replace("\"", "").split(",")
			return img.map { url ->
				MangaPage(
					id = generateUid(url),
					url = url,
					preview = null,
					source = source,
				)
			}
		} else {
			return doc.body().selectFirstOrThrow(selectBodyPage).select(selectPage).map { div ->
				val img = div.selectFirstOrThrow("img")
				val url = img.requireSrc().toRelativeUrl(domain)
				MangaPage(
					id = generateUid(url),
					url = url,
					preview = null,
					source = source,
				)
			}
		}

	}
}
