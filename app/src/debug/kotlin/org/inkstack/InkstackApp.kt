package org.inkstack

import android.content.Context
import android.content.SharedPreferences
import android.os.StrictMode
import androidx.core.content.edit
import androidx.fragment.app.strictmode.FragmentStrictMode
import leakcanary.LeakCanary
import org.inkstack.core.BaseApp

class InkstackApp : BaseApp() {

	var isLeakCanaryEnabled: Boolean
		get() = getDebugPreferences(this).getBoolean(KEY_LEAK_CANARY, true)
		set(value) {
			getDebugPreferences(this).edit { putBoolean(KEY_LEAK_CANARY, value) }
			configureLeakCanary()
		}

	override fun onCreate() {
		org.inkstack.settings.DebugSettingsAccess.register { onBack ->
			org.inkstack.settings.DebugSettingsScreen(onBack = onBack)
		}
		super.onCreate()
	}

	override fun attachBaseContext(base: Context) {
		super.attachBaseContext(base)
		enableStrictMode()
		configureLeakCanary()
	}

	private fun configureLeakCanary() {
		LeakCanary.config = LeakCanary.config.copy(
			dumpHeap = isLeakCanaryEnabled,
		)
	}

	private fun enableStrictMode() {
		val notifier = StrictModeNotifier(this)
		StrictMode.setThreadPolicy(
			StrictMode.ThreadPolicy.Builder().apply {
				detectNetwork()
				detectDiskWrites()
				detectCustomSlowCalls()
				detectResourceMismatches()
				detectUnbufferedIo()
				detectExplicitGc()
				penaltyLog()
				penaltyListener(notifier.executor, notifier)
			}.build(),
		)
		StrictMode.setVmPolicy(
			StrictMode.VmPolicy.Builder().apply {
				detectActivityLeaks()
				detectLeakedSqlLiteObjects()
				detectLeakedClosableObjects()
				detectLeakedRegistrationObjects()
				detectContentUriWithoutPermission()
				detectFileUriExposure()
				penaltyLog()
				penaltyListener(notifier.executor, notifier)
			}.build(),
		)
		FragmentStrictMode.defaultPolicy = FragmentStrictMode.Policy.Builder().apply {
			detectWrongFragmentContainer()
			detectFragmentTagUsage()
			detectRetainInstanceUsage()
			detectSetUserVisibleHint()
			detectWrongNestedHierarchy()
			detectFragmentReuse()
			penaltyLog()
			penaltyListener(notifier)
		}.build()
	}

	private companion object {

		const val PREFS_DEBUG = "_debug"
		const val KEY_LEAK_CANARY = "leak_canary"

		fun getDebugPreferences(context: Context): SharedPreferences =
			context.getSharedPreferences(PREFS_DEBUG, MODE_PRIVATE)
	}
}
