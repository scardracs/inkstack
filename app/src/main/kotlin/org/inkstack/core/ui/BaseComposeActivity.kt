package org.inkstack.core.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.inkstack.BuildConfig
import org.inkstack.R
import org.inkstack.core.exceptions.resolve.ExceptionResolver
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.ui.compose.InkstackTheme
import org.inkstack.core.ui.util.ActionModeDelegate
import org.inkstack.main.ui.protect.ScreenshotPolicyHelper

/**
 * Compose-first activity base. Mirrors [BaseActivity] theme / exception / NSFW hooks
 * without requiring ViewBinding.
 */
abstract class BaseComposeActivity :
	AppCompatActivity(),
	ScreenshotPolicyHelper.ContentContainer {

	private var isAmoledTheme = false

	protected lateinit var exceptionResolver: ExceptionResolver
		private set

	@JvmField
	val actionModeDelegate = ActionModeDelegate()

	private lateinit var entryPoint: BaseActivityEntryPoint

	protected open val applyColorSchemeTheme: Boolean = true

	override fun attachBaseContext(newBase: Context) {
		entryPoint = EntryPointAccessors.fromApplication<BaseActivityEntryPoint>(newBase.applicationContext)
		super.attachBaseContext(newBase)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		val settings = entryPoint.settings
		isAmoledTheme = settings.isAmoledTheme
		if (applyColorSchemeTheme) {
			setTheme(settings.colorScheme.styleResId)
			if (isAmoledTheme) {
				setTheme(R.style.ThemeOverlay_Inkstack_Amoled)
			}
			enableEdgeToEdge()
		}
		putDataToExtras(intent)
		exceptionResolver = entryPoint.exceptionResolverFactory.create(this)
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)
	}

	override fun onPostCreate(savedInstanceState: Bundle?) {
		super.onPostCreate(savedInstanceState)
		onBackPressedDispatcher.addCallback(actionModeDelegate)
	}

	override fun onNewIntent(intent: Intent) {
		putDataToExtras(intent)
		super.onNewIntent(intent)
	}

	protected fun setComposeContent(content: @Composable () -> Unit) {
		setContent {
			InkstackTheme(content = content)
		}
	}

	override fun onSupportNavigateUp(): Boolean {
		dispatchNavigateUp()
		return true
	}

	override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
		if (BuildConfig.DEBUG) {
			if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
				ActivityCompat.recreate(this)
				return true
			} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
				throw RuntimeException("Test crash")
			}
		}
		return super.onKeyDown(keyCode, event)
	}

	protected fun isDarkAmoledTheme(): Boolean {
		val uiMode = resources.configuration.uiMode
		val isNight = uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
		return isNight && isAmoledTheme
	}

	@CallSuper
	override fun onSupportActionModeStarted(mode: ActionMode) {
		super.onSupportActionModeStarted(mode)
		actionModeDelegate.onSupportActionModeStarted(mode, window)
	}

	@CallSuper
	override fun onSupportActionModeFinished(mode: ActionMode) {
		super.onSupportActionModeFinished(mode)
		actionModeDelegate.onSupportActionModeFinished(mode, window)
	}

	protected open fun dispatchNavigateUp() {
		val upIntent = parentActivityIntent
		if (upIntent != null) {
			if (!navigateUpTo(upIntent)) {
				startActivity(upIntent)
			}
		} else {
			finishAfterTransition()
		}
	}

	override fun isNsfwContent(): Flow<Boolean> = flowOf(false)

	private fun putDataToExtras(intent: Intent?) {
		intent?.putExtra(AppRouter.KEY_DATA, intent.data)
	}
}
