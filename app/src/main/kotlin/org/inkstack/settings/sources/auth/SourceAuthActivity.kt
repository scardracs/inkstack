package org.inkstack.settings.sources.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.inkstack.R
import org.inkstack.browser.BaseBrowserActivity
import org.inkstack.browser.BrowserCallback
import org.inkstack.browser.BrowserClient
import org.inkstack.core.model.getTitle
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.parser.ParserMangaRepository
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.parsers.MangaParserAuthProvider
import org.inkstack.parsers.model.MangaSource
import org.inkstack.parsers.util.runCatchingCancellable

@AndroidEntryPoint
class SourceAuthActivity : BaseBrowserActivity(), BrowserCallback {

	private lateinit var authProvider: MangaParserAuthProvider

	private var authCheckJob: Job? = null

	override fun onCreate2(savedInstanceState: Bundle?, source: MangaSource, repository: ParserMangaRepository?) {
		if (repository == null) {
			finishAfterTransition()
			return
		}
		authProvider = repository.getAuthProvider() ?: run {
			Toast.makeText(
				this,
				getString(R.string.auth_not_supported_by, source.getTitle(this)),
				Toast.LENGTH_SHORT,
			).show()
			finishAfterTransition()
			return
		}
		showCloseNavigation = true
		webView.webViewClient = BrowserClient(this)
		lifecycleScope.launch {
			try {
				proxyProvider.applyWebViewConfig()
			} catch (e: Exception) {
				snackbarHostState.showSnackbar(e.getDisplayMessage(resources))
			}
			if (savedInstanceState == null) {
				val url = authProvider.authUrl
				onTitleChanged(
					source.getTitle(this@SourceAuthActivity),
					getString(R.string.loading_),
				)
				webView.loadUrl(url)
			}
		}
	}

	override fun onNavigationClose() {
		webView.stopLoading()
		setResult(RESULT_CANCELED)
		finishAfterTransition()
	}

	override fun onLoadingStateChanged(isLoading: Boolean) {
		super.onLoadingStateChanged(isLoading)
		if (isLoading) {
			return
		}
		val prevJob = authCheckJob
		authCheckJob = lifecycleScope.launch {
			prevJob?.join()
			val isAuthorized = runCatchingCancellable {
				authProvider.isAuthorized()
			}.getOrDefault(false)
			if (isAuthorized) {
				Toast.makeText(this@SourceAuthActivity, R.string.auth_complete, Toast.LENGTH_SHORT).show()
				setResult(RESULT_OK)
				finishAfterTransition()
			}
		}
	}

	class Contract : ActivityResultContract<MangaSource, Boolean>() {

		override fun createIntent(context: Context, input: MangaSource) = AppRouter.sourceAuthIntent(context, input)

		override fun parseResult(resultCode: Int, intent: Intent?) = resultCode == RESULT_OK
	}

	companion object {
		const val TAG = "SourceAuthActivity"
	}
}
