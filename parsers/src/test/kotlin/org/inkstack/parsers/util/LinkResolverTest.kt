package org.inkstack.parsers.util

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import org.inkstack.parsers.MangaLoaderContextMock
import org.inkstack.parsers.model.MangaParserSource
import kotlin.time.Duration.Companion.minutes

internal class LinkResolverTest {

	private val context = MangaLoaderContextMock

	@Test
	fun supportedSource() = runTest(timeout = 2.minutes) {
		val url = "https://mangadex.org/title/00000000-0000-4000-8000-000000000001"
		val resolver = context.newLinkResolver(url)
		Assertions.assertEquals(MangaParserSource.MANGADEX, resolver.getSource())
		val manga = runCatching { resolver.getManga() }.getOrNull()
		Assumptions.assumeTrue(manga != null, "Live MangaDex title required for getManga resolution")
		Assertions.assertEquals(resolver.link.toString(), requireNotNull(manga).publicUrl)
	}

	@Test
	fun sharedDomainPrefersWorkingSourceWithMatchingLocale() = runTest {
		val cases = mapOf(
			"https://www.webtoons.com/en/fantasy/example/list?title_no=123" to MangaParserSource.WEBTOONS_EN,
			"https://www.webtoons.com/fr/fantasy/example/list?title_no=123" to MangaParserSource.WEBTOONS_FR,
			"https://www.webtoons.com/de/canvas/example/list?title_no=123" to MangaParserSource.WEBTOONS_DE,
			"https://m.webtoons.com/zh-hant/fantasy/example/ep-1/viewer?title_no=123" to MangaParserSource.WEBTOONS_ZH,
			"https://webtoons.com/id/fantasy/example/list?title_no=123" to MangaParserSource.WEBTOONS_ID,
		)
		for ((url, expected) in cases) {
			Assertions.assertEquals(expected, context.newLinkResolver(url).getSource(), url)
		}
	}

	@Test
	fun exactHostWinsOverSharedTopDomain() = runTest {
		val cases = mapOf(
			"https://www.niadd.com/manga/example.html" to MangaParserSource.NINEMANGA_EN,
			"https://es.niadd.com/manga/example.html" to MangaParserSource.NINEMANGA_ES,
			"https://br.niadd.com/manga/example.html" to MangaParserSource.NINEMANGA_BR,
			"https://fr.niadd.com/manga/example.html" to MangaParserSource.NINEMANGA_FR,
		)
		for ((url, expected) in cases) {
			Assertions.assertEquals(expected, context.newLinkResolver(url).getSource(), url)
		}
	}

	@Test
	fun brokenSourceDomainStillResolves() = runTest {
		val url = "https://xcat.tv/title/0000000000000000"
		val resolver = context.newLinkResolver(url)
		Assertions.assertEquals(MangaParserSource.XBATCAT, resolver.getSource())
	}
}
