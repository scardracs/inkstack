package org.inkstack.core.crash

import android.content.Context
import kotlin.system.exitProcess

/**
 * Writes a pending crash report then chains to the previous handler / kills the process.
 * Consent + GitHub upload happen on the next cold start via [CrashReportActivity].
 */
class GlobalExceptionHandler(
	private val appContext: Context,
	private val previous: Thread.UncaughtExceptionHandler?,
) : Thread.UncaughtExceptionHandler {

	override fun uncaughtException(thread: Thread, throwable: Throwable) {
		runCatching {
			CrashReportStore.save(appContext, throwable, GitHubIssuesReporter.Kind.CRASH)
		}
		if (previous != null) {
			previous.uncaughtException(thread, throwable)
		} else {
			exitProcess(10)
		}
	}

	companion object {
		fun install(context: Context) {
			val app = context.applicationContext
			val previous = Thread.getDefaultUncaughtExceptionHandler()
			if (previous is GlobalExceptionHandler) return
			Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler(app, previous))
		}
	}
}
