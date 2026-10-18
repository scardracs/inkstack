package org.inkstack.main.ui.protect

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.prefs.ScreenshotsPolicy
import org.inkstack.core.prefs.observeAsFlow
import org.inkstack.core.ui.DefaultActivityLifecycleCallbacks
import org.inkstack.settings.protect.ProtectSetupActivity
import javax.inject.Inject

class ScreenshotPolicyHelper @Inject constructor(
	private val settings: AppSettings,
) : DefaultActivityLifecycleCallbacks {

	override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
		(activity as? ContentContainer)?.setupScreenshotPolicy(activity)
	}

	private fun ContentContainer.setupScreenshotPolicy(activity: Activity) =
		lifecycleScope.launch(Dispatchers.Default) {
			settings.observeAsFlow(AppSettings.KEY_SCREENSHOTS_POLICY) { screenshotsPolicy }
				.flatMapLatest { policy ->
					when (policy) {
						ScreenshotsPolicy.ALLOW -> flowOf(false)
						ScreenshotsPolicy.BLOCK_NSFW -> withContext(Dispatchers.Main) {
							isNsfwContent()
						}.distinctUntilChanged()

						ScreenshotsPolicy.BLOCK_ALL -> flowOf(true)
						ScreenshotsPolicy.BLOCK_INCOGNITO -> settings.observeAsFlow(AppSettings.KEY_INCOGNITO_MODE) {
							isIncognitoModeEnabled
						}
					}
				}.collect { isSecure ->
					withContext(Dispatchers.Main) {
						val keepSecure = activity is ProtectActivity || activity is ProtectSetupActivity
						if (isSecure || keepSecure) {
							activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
						} else {
							activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
						}
					}
				}
		}

	interface ContentContainer : LifecycleOwner {

		@MainThread
		fun isNsfwContent(): Flow<Boolean>
	}
}
