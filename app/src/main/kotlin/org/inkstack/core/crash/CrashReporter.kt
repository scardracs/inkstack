package org.inkstack.core.crash

import android.app.Application
import android.content.Context
import kotlinx.coroutines.launch
import org.inkstack.core.exceptions.CaughtException
import org.inkstack.core.util.ext.printStackTraceDebug
import org.inkstack.core.util.ext.processLifecycleScope

/**
 * App-wide crash / error reporting entry point (GitHub Issues).
 */
object CrashReporter {

	@Volatile
	private var app: Application? = null

	fun init(application: Application) {
		app = application
		GlobalExceptionHandler.install(application)
		val pending = CrashReportStore.peek(application)
		if (pending != null && GitHubIssuesReporter.isEnabled) {
			application.startActivity(CrashReportActivity.intentForPending(application))
		}
	}

	fun report(throwable: Throwable, silent: Boolean = false) {
		val wrapped = CaughtException(throwable)
		wrapped.printStackTraceDebug()
		val context = app ?: return
		if (!GitHubIssuesReporter.isEnabled) return
		val kind = GitHubIssuesReporter.Kind.IN_APP
		if (silent) {
			processLifecycleScope.launch {
				GitHubIssuesReporter.submit(
					context,
					GitHubIssuesReporter.Report.from(wrapped, kind),
				)
			}
		} else {
			context.startActivity(CrashReportActivity.intent(context, wrapped, kind))
		}
	}

	fun requireContext(): Context =
		app ?: error("CrashReporter not initialized")
}
