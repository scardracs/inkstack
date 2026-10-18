package org.inkstack.core.network

import dagger.Lazy
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.inkstack.BuildConfig
import org.inkstack.core.model.MangaSource
import org.inkstack.core.parser.MangaLoaderContextImpl
import org.inkstack.core.parser.MangaRepository
import org.inkstack.core.parser.ParserMangaRepository
import org.inkstack.core.util.ext.printStackTraceDebug
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.model.MangaSource
import org.inkstack.parsers.util.mergeWith
import org.inkstack.parsers.util.runCatchingCancellable
import java.net.IDN
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommonHeadersInterceptor @Inject constructor(
	private val mangaRepositoryFactoryLazy: Lazy<MangaRepository.Factory>,
	private val mangaLoaderContextLazy: Lazy<MangaLoaderContextImpl>,
) : Interceptor {

	override fun intercept(chain: Chain): Response {
		val request = chain.request()
		val source = request.tag(MangaSource::class.java)
			?: request.headers[CommonHeaders.MANGA_SOURCE]?.let { MangaSource(it) }
		val repository = if (source is MangaParserSource) {
			mangaRepositoryFactoryLazy.get().create(source) as? ParserMangaRepository
		} else {
			if (BuildConfig.DEBUG && source == null) {
				IllegalArgumentException("Request without source tag: ${request.url}")
					.printStackTrace()
			}
			null
		}
		val headersBuilder = request.headers.newBuilder()
			.removeAll(CommonHeaders.MANGA_SOURCE)
		repository?.getRequestHeaders()?.let {
			headersBuilder.mergeWith(it, replaceExisting = false)
		}
		// Overrides the parser's agent, deliberately: a cf_clearance cookie is only honoured for the
		// exact User-Agent it was issued to, so once a challenge has been solved the request must
		// present that same identity or it stays blocked forever. See SourceSettings.cloudFlareUserAgent.
		repository?.getConfig()?.cloudFlareUserAgent?.let {
			headersBuilder.trySet(CommonHeaders.USER_AGENT, it)
		}
		if (headersBuilder[CommonHeaders.USER_AGENT] == null) {
			headersBuilder[CommonHeaders.USER_AGENT] = mangaLoaderContextLazy.get().getDefaultUserAgent()
		}
		if (headersBuilder[CommonHeaders.REFERER] == null && repository != null) {
			val idn = IDN.toASCII(repository.domain)
			headersBuilder.trySet(CommonHeaders.REFERER, "https://$idn/")
		}
		val newRequest = request.newBuilder().headers(headersBuilder.build()).build()
		return repository?.interceptSafe(ProxyChain(chain, newRequest)) ?: chain.proceed(newRequest)
	}

	private fun Headers.Builder.trySet(name: String, value: String) = try {
		set(name, value)
	} catch (e: IllegalArgumentException) {
		e.printStackTraceDebug()
	}

	private fun Interceptor.interceptSafe(chain: Chain): Response = runCatchingCancellable {
		intercept(chain)
	}.getOrElse { e ->
		if (e is IOException || e is Error) {
			throw e
		} else {
			// only IOException can be safely thrown from an Interceptor
			throw IOException("Error in interceptor: ${e.message}", e)
		}
	}

	private class ProxyChain(
		private val delegate: Chain,
		private val request: Request,
	) : Chain by delegate {

		override fun request(): Request = request
	}
}
