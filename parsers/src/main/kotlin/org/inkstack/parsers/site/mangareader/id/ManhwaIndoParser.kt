package org.inkstack.parsers.site.mangareader.id

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaChapter
import org.inkstack.parsers.model.MangaPage
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser
import org.inkstack.parsers.util.*

@MangaSourceParser("MANHWAINDO", "ManhwaIndo", "id")
internal class ManhwaIndoParser(context: MangaLoaderContext) :
    MangaReaderParser(context, MangaParserSource.MANHWAINDO, "www.manhwaindo.my", pageSize = 30, searchPageSize = 20) {
    override val listUrl = "/series"
    override val selectMangaList = "div.bs"
    override val selectMangaListImg = "img"
    override val selectMangaListTitle = ".tt"
    override val selectChapter = "#chapterlist li"
    override val selectPage = "#readerarea img"

    override suspend fun getPages(chapter: MangaChapter): List<MangaPage> {
        val chapterUrl = chapter.url.toAbsoluteUrl(domain)
        val doc = webClient.httpGet(chapterUrl).parseHtml()
        return doc.select(selectPage)
            .filter { it.hasAttr("src") || it.hasAttr("data-src") }
            .map { img ->
                val imageUrl = img.src()?.toAbsoluteUrl(domain) ?: ""
                MangaPage(
                    id = generateUid(imageUrl),
                    url = imageUrl,
                    preview = null,
                    source = source
                )
            }
            .filter { it.url.isNotBlank() && !it.url.contains("cover") }
    }
}
