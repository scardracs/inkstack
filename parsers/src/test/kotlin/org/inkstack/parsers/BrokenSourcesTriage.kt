package org.inkstack.parsers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.model.search.MangaSearchQuery
import org.inkstack.parsers.util.await
import java.io.File
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes

/**
 * Triage all [MangaParserSource.isBroken] parsers: domain liveness + optional getList smoke.
 *
 *     BROKEN_TRIAGE_OUT=parsers/broken-triage.tsv \
 *     BROKEN_TRIAGE_SMOKE=1 \
 *     ./gradlew :parsers:test --tests '*BrokenSourcesTriage*' -i
 *
 * Probe-only (faster): omit BROKEN_TRIAGE_SMOKE.
 */
internal class BrokenSourcesTriage {

	private val context = MangaLoaderContextMock
	private val probeClient = context.httpClient.newBuilder()
		.followRedirects(true)
		.connectTimeout(12, TimeUnit.SECONDS)
		.readTimeout(20, TimeUnit.SECONDS)
		.callTimeout(25, TimeUnit.SECONDS)
		.build()

	@Test
	fun triage() = runTest(timeout = 90.minutes) {
		val out = System.getenv("BROKEN_TRIAGE_OUT")
		assumeTrue(out != null, "BROKEN_TRIAGE_OUT is not set")
		val doSmoke = System.getenv("BROKEN_TRIAGE_SMOKE") == "1"
		val reasons = loadReasons()
		val broken = MangaParserSource.entries.filter { it.isBroken }
		println("Broken sources: ${broken.size}; smoke=$doSmoke")

		val concurrency = Semaphore(permits = 12)
		val rows = coroutineScope {
			broken.map { source ->
				async(Dispatchers.IO) {
					concurrency.withPermit { triageOne(source, reasons[source.name].orEmpty(), doSmoke) }
				}
			}.awaitAll()
		}.sortedBy { it.source }

		val file = File(out!!)
		file.parentFile?.mkdirs()
		file.writeText(buildString {
			appendLine("source\tdomain\treason\tprobe\thttp\tfinal_host\tsmoke\terror")
			for (row in rows) {
				appendLine(
					listOf(
						row.source,
						row.domain,
						escape(row.reason),
						row.probe,
						row.httpCode?.toString().orEmpty(),
						row.finalHost,
						row.smoke,
						escape(row.error),
					).joinToString("\t"),
				)
			}
		})

		val byProbe = rows.groupingBy { it.probe }.eachCount()
		val bySmoke = rows.groupingBy { it.smoke }.eachCount()
		println("Wrote ${rows.size} rows to $out")
		println("probe: $byProbe")
		println("smoke: $bySmoke")
		val revive = rows.filter { it.smoke == "PASS" }.map { it.source }
		if (revive.isNotEmpty()) {
			println("Candidates to un-@Broken (${revive.size}):")
			revive.forEach { println("  $it") }
		}
	}

	private suspend fun triageOne(source: MangaParserSource, reason: String, doSmoke: Boolean): Row {
		val parser = runCatching { context.newParserInstance(source) }.getOrElse { e ->
			return Row(
				source = source.name,
				domain = "",
				reason = reason,
				probe = "INSTANTIATE_FAIL",
				httpCode = null,
				finalHost = "",
				smoke = "SKIP",
				error = e.message.orEmpty(),
			)
		}
		val domain = runCatching { parser.domain }.getOrElse { e ->
			return Row(
				source = source.name,
				domain = "",
				reason = reason,
				probe = "NO_DOMAIN",
				httpCode = null,
				finalHost = "",
				smoke = "SKIP",
				error = e.message.orEmpty(),
			)
		}
		val url = "https://$domain/".toHttpUrlOrNull()?.toString()
			?: return Row(source.name, domain, reason, "BAD_DOMAIN", null, "", "SKIP", "invalid domain")

		val probe = runCatching {
			val response = probeClient.newCall(Request.Builder().url(url).get().build()).await()
			response.use { resp ->
				val code = resp.code
				val host = resp.request.url.host
				val bodyPeek = resp.peekBody(8_192).string()
				val probeLabel = when {
					code in 200..399 && looksLikeCloudflare(bodyPeek) -> "CF"
					code in 200..399 -> "ALIVE"
					code == 403 || code == 503 -> if (looksLikeCloudflare(bodyPeek)) "CF" else "HTTP_$code"
					code == 404 || code == 410 -> "DEAD_HTTP"
					else -> "HTTP_$code"
				}
				ProbeResult(probeLabel, code, host)
			}
		}.getOrElse { e ->
			val label = when (e) {
				is UnknownHostException -> "DEAD_DNS"
				is CloudFlareProtectedException -> "CF"
				else -> when {
					e.message?.contains("CloudFlare", ignoreCase = true) == true -> "CF"
					e.message?.contains("Unable to resolve", ignoreCase = true) == true -> "DEAD_DNS"
					e.message?.contains("timeout", ignoreCase = true) == true -> "TIMEOUT"
					else -> "ERROR"
				}
			}
			ProbeResult(label, null, "", e.message.orEmpty())
		}

		var smoke = "SKIP"
		var smokeErr = probe.error
		if (doSmoke && probe.label == "ALIVE") {
			val smokeResult = runCatching {
				val list = parser.getList(MangaSearchQuery.EMPTY)
				check(list.isNotEmpty()) { "empty list" }
				"PASS"
			}.getOrElse { e ->
				smokeErr = e.message.orEmpty()
				when {
					e is CloudFlareProtectedException -> "CF"
					e.message?.contains("CloudFlare", ignoreCase = true) == true -> "CF"
					else -> "FAIL"
				}
			}
			smoke = smokeResult
		}

		return Row(
			source = source.name,
			domain = domain,
			reason = reason,
			probe = probe.label,
			httpCode = probe.code,
			finalHost = probe.host,
			smoke = smoke,
			error = smokeErr.take(200),
		)
	}

	private fun looksLikeCloudflare(html: String): Boolean {
		val h = html.lowercase()
		return "cf-browser-verification" in h ||
			"just a moment" in h ||
			"cf-challenge" in h ||
			"cdn-cgi/challenge" in h ||
			"_cf_chl" in h
	}

	private fun loadReasons(): Map<String, String> {
		val map = LinkedHashMap<String, String>()
		val stream = javaClass.getResourceAsStream("/broken_reasons.tsv")
			?: File("/tmp/broken_reasons.tsv").takeIf { it.isFile }?.inputStream()
			?: return emptyMap()
		stream.bufferedReader().useLines { lines ->
			for (line in lines) {
				val parts = line.split('\t', limit = 3)
				if (parts.size >= 2 && parts[0] != "?") {
					map[parts[0]] = parts[1]
				}
			}
		}
		return map
	}

	private fun escape(value: String): String =
		value.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ')

	private data class ProbeResult(
		val label: String,
		val code: Int?,
		val host: String,
		val error: String = "",
	)

	private data class Row(
		val source: String,
		val domain: String,
		val reason: String,
		val probe: String,
		val httpCode: Int?,
		val finalHost: String,
		val smoke: String,
		val error: String,
	)
}
