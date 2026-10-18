package org.inkstack.parsers.site.madara.en

import okhttp3.Interceptor
import okhttp3.Response
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("ARVENSCANS", "ArvenComics", "en")
internal class ArvenScans(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.ARVENSCANS, "arvencomics.com") {

	override val listUrl = "comic/"
    override val withoutAjax = true

	override fun intercept(chain: Interceptor.Chain): Response {
		synchronized(rateLimitLock) {
			val now = System.currentTimeMillis()
			val waitMs = REQUEST_INTERVAL_MS - (now - lastRequestAt)
			if (waitMs > 0) {
				Thread.sleep(waitMs)
			}
			lastRequestAt = System.currentTimeMillis()
		}
		return super.intercept(chain)
	}

	private companion object {
		private const val REQUEST_INTERVAL_MS = 1_000L
		private var lastRequestAt = 0L
		private val rateLimitLock = Any()
	}
}
