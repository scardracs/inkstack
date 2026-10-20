package org.inkstack.core.crash

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * Persists a crash report across process death so the consent UI can run on the next launch.
 */
object CrashReportStore {

	private const val DIR = "crash_reports"
	private const val PENDING = "pending.json"

	data class PendingReport(
		val id: String,
		val stackTrace: String,
		val exceptionClass: String,
		val message: String?,
		val kind: GitHubIssuesReporter.Kind,
		val createdAt: Long,
	)

	fun save(context: Context, throwable: Throwable, kind: GitHubIssuesReporter.Kind) {
		val dir = dir(context)
		dir.mkdirs()
		val payload = JSONObject()
			.put("id", UUID.randomUUID().toString())
			.put("stackTrace", throwable.stackTraceToString().take(60_000))
			.put("exceptionClass", throwable.javaClass.name)
			.put("message", throwable.message)
			.put("kind", kind.name)
			.put("createdAt", System.currentTimeMillis())
		File(dir, PENDING).writeText(payload.toString())
	}

	fun peek(context: Context): PendingReport? {
		val file = File(dir(context), PENDING)
		if (!file.isFile) return null
		return runCatching {
			val json = JSONObject(file.readText())
			PendingReport(
				id = json.getString("id"),
				stackTrace = json.getString("stackTrace"),
				exceptionClass = json.getString("exceptionClass"),
				message = json.optString("message").ifEmpty { null },
				kind = GitHubIssuesReporter.Kind.valueOf(json.getString("kind")),
				createdAt = json.getLong("createdAt"),
			)
		}.getOrNull()
	}

	fun clear(context: Context) {
		File(dir(context), PENDING).delete()
	}

	private fun dir(context: Context): File = File(context.filesDir, DIR)
}
