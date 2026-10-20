package org.inkstack.core.crash

import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.inkstack.BuildConfig
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Creates GitHub issues on [BuildConfig.GITHUB_ISSUES_REPO] for crash / in-app error reports.
 *
 * Requires [BuildConfig.GITHUB_ISSUES_TOKEN] (fine-grained PAT with Issues: Write on that repo only).
 * When the token is empty, reporting is a no-op.
 */
object GitHubIssuesReporter {

	private val jsonMedia = "application/json; charset=utf-8".toMediaType()

	private val client by lazy {
		OkHttpClient.Builder()
			.connectTimeout(20, TimeUnit.SECONDS)
			.readTimeout(30, TimeUnit.SECONDS)
			.writeTimeout(30, TimeUnit.SECONDS)
			.build()
	}

	val isEnabled: Boolean
		get() = BuildConfig.GITHUB_ISSUES_TOKEN.isNotEmpty() &&
			BuildConfig.GITHUB_ISSUES_REPO.contains('/')

	enum class Kind {
		CRASH,
		IN_APP,
		ANR,
	}

	data class Report(
		val exceptionClass: String,
		val message: String?,
		val stackTrace: String,
		val kind: Kind,
		val userMessage: String? = null,
	) {
		companion object {
			fun from(throwable: Throwable, kind: Kind, userMessage: String? = null) = Report(
				exceptionClass = throwable.javaClass.name,
				message = throwable.message,
				stackTrace = throwable.stackTraceToString().take(60_000),
				kind = kind,
				userMessage = userMessage,
			)

			fun from(pending: CrashReportStore.PendingReport) = Report(
				exceptionClass = pending.exceptionClass,
				message = pending.message,
				stackTrace = pending.stackTrace,
				kind = pending.kind,
			)
		}
	}

	suspend fun submit(context: Context, report: Report): Result<String> = withContext(Dispatchers.IO) {
		if (!isEnabled) {
			return@withContext Result.failure(IllegalStateException("GitHub issues reporting is not configured"))
		}
		runCatching {
			val labels = buildLabels(report.kind)
			val title = buildTitle(report)
			val body = buildBody(report)
			val payload = JSONObject()
				.put("title", title.take(240))
				.put("body", body)
				.put("labels", JSONArray(labels))
			val repo = BuildConfig.GITHUB_ISSUES_REPO
			val request = Request.Builder()
				.url("https://api.github.com/repos/$repo/issues")
				.header("Accept", "application/vnd.github+json")
				.header("Authorization", "Bearer ${BuildConfig.GITHUB_ISSUES_TOKEN}")
				.header("X-GitHub-Api-Version", "2022-11-28")
				.header("User-Agent", "Inkstack/${BuildConfig.VERSION_NAME}")
				.post(payload.toString().toRequestBody(jsonMedia))
				.build()
			client.newCall(request).execute().use { response ->
				val responseBody = response.body.string()
				if (!response.isSuccessful) {
					error("GitHub API ${response.code}: ${responseBody.take(400)}")
				}
				val htmlUrl = JSONObject(responseBody).optString("html_url")
				htmlUrl.ifEmpty { "https://github.com/$repo/issues" }
			}
		}
	}

	private fun buildLabels(kind: Kind): List<String> = buildList {
		add("android")
		add("needs-triage")
		when (kind) {
			Kind.CRASH -> {
				add("crash")
				add("bug")
			}
			Kind.IN_APP -> {
				add("reported-from-app")
				add("bug")
			}
			Kind.ANR -> {
				add("anr")
				add("bug")
			}
		}
	}

	private fun buildTitle(report: Report): String {
		val type = report.exceptionClass.substringAfterLast('.')
		val message = report.message?.lineSequence()?.firstOrNull()?.take(120).orEmpty()
		val prefix = when (report.kind) {
			Kind.CRASH -> "Crash"
			Kind.IN_APP -> "Error"
			Kind.ANR -> "ANR"
		}
		return if (message.isEmpty()) "$prefix: $type" else "$prefix: $type — $message"
	}

	private fun buildBody(report: Report): String = buildString {
		appendLine("### Summary")
		appendLine()
		appendLine(
			when (report.kind) {
				Kind.CRASH -> "Uncaught crash reported by Inkstack."
				Kind.IN_APP -> "Error report submitted from the app."
				Kind.ANR -> "ANR report from Inkstack."
			},
		)
		if (!report.userMessage.isNullOrBlank()) {
			appendLine()
			appendLine("### User note")
			appendLine()
			appendLine(report.userMessage.trim())
		}
		appendLine()
		appendLine("### Environment")
		appendLine()
		appendLine("| | |")
		appendLine("|---|---|")
		appendLine("| App | `${BuildConfig.APPLICATION_ID}` `${BuildConfig.VERSION_NAME}` (`${BuildConfig.VERSION_CODE}`) |")
		appendLine("| Build | `${BuildConfig.BUILD_TYPE}` |")
		appendLine("| Android | `${Build.VERSION.RELEASE}` (API ${Build.VERSION.SDK_INT}) |")
		appendLine("| Device | `${Build.MANUFACTURER} ${Build.MODEL}` |")
		appendLine("| Locale | `${Locale.getDefault()}` |")
		appendLine()
		appendLine("### Stack trace")
		appendLine()
		appendLine("```")
		appendLine(report.stackTrace)
		appendLine("```")
		appendLine()
		appendLine("_Submitted automatically by Inkstack. Please do not include screenshots of copyrighted pages._")
	}
}
