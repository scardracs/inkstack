package org.inkstack.parsers.site.mangahub.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.config.ConfigKey
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangahub.MangaHubParser

@Broken("BLOCKED BY CLOUDFARE")

@MangaSourceParser("MANGAHUB_IO", "MangaHubIO", "en")
internal class MangaHubIo(context: MangaLoaderContext) :
	MangaHubParser(context, MangaParserSource.MANGAHUB_IO, "mangahub.io", "m01") {
    override fun onCreateConfig(keys: MutableCollection<ConfigKey<*>>) {
        super.onCreateConfig(keys)
        keys.add(ConfigKey.InterceptCloudflare(defaultValue = true))
    }
}
