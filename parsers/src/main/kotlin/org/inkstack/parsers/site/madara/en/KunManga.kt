package org.inkstack.parsers.site.madara.en

import okhttp3.Headers
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.config.ConfigKey
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("KUNMANGA", "KunManga", "en")
internal class KunManga(context: MangaLoaderContext) :
    MadaraParser(context, MangaParserSource.KUNMANGA, "kunmanga.com", 10) {
    override val withoutAjax = true
    override fun onCreateConfig(keys: MutableCollection<ConfigKey<*>>) {
        super.onCreateConfig(keys)
        keys.add(ConfigKey.InterceptCloudflare(defaultValue = true))
    }
}
