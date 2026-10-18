package org.inkstack.parsers.site.natsu.id

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.config.ConfigKey
import org.inkstack.parsers.model.MangaPage
import org.inkstack.parsers.model.MangaChapter
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.natsu.NatsuParser
import org.inkstack.parsers.util.generateUid
import org.inkstack.parsers.util.parseHtml
import org.inkstack.parsers.util.requireSrc
import org.inkstack.parsers.util.toAbsoluteUrl
import org.inkstack.parsers.util.toRelativeUrl

@MangaSourceParser("KIRYUU", "Kiryuu", "id")
internal class Kiryuu(context: MangaLoaderContext) :
    NatsuParser(context, MangaParserSource.KIRYUU, 24) {
    override val configKeyDomain = ConfigKey.Domain("v6.kiryuu.to")

    override suspend fun getPages(chapter: MangaChapter): List<MangaPage> {
        val doc = webClient.httpGet(chapter.url.toAbsoluteUrl(domain)).parseHtml()
        // Images are in a section with data-image-data attribute
        return doc.select("section[data-image-data] img").map { img ->
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
