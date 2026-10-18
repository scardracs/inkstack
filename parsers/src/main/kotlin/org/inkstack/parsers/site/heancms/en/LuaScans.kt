package org.inkstack.parsers.site.heancms.en

import org.json.JSONObject
import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.*
import org.inkstack.parsers.site.heancms.HeanCms
import org.inkstack.parsers.util.generateUid
import org.inkstack.parsers.util.json.asTypedList
import org.inkstack.parsers.util.mapChapters
import org.inkstack.parsers.util.parseJson
import org.inkstack.parsers.util.parseSafe
import java.text.SimpleDateFormat
import java.util.*

@Broken
@MangaSourceParser("LUACOMIC_COM", "Lua Scans", "en")
internal class LuaScans(context: MangaLoaderContext) :
    HeanCms(context, MangaParserSource.LUACOMIC_COM, "luacomic.com") {

    override val datePattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    override suspend fun getDetails(manga: Manga): Manga {
        val seriesId = manga.url.toLongOrNull() ?: manga.id
        val url = reqUrl(seriesId)
        val response = webClient.httpGet(url).parseJson()
        val data = response.getJSONArray("data").asTypedList<JSONObject>()
        val dateFormat = SimpleDateFormat(datePattern, Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return manga.copy(
            chapters = data.mapChapters(reversed = true) { i, it ->
                val chapterUrl = "/series/${it.getJSONObject("series").getString("series_slug")}/${it.getString("chapter_slug")}"
                MangaChapter(
                    id = generateUid(it.getLong("id")),
                    title = it.getString("chapter_name"),
                    number = i + 1f,
                    volume = 0,
                    url = chapterUrl,
                    scanlator = null,
                    uploadDate = dateFormat.parseSafe(it.getString("created_at")),
                    branch = null,
                    source = source,
                )
            },
        )
    }
}
