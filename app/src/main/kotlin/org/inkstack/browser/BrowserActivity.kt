package org.inkstack.browser

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.inkstack.R
import org.inkstack.core.exceptions.InteractiveActionRequiredException
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.nav.router
import org.inkstack.core.parser.ParserMangaRepository
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.core.util.ext.printStackTraceDebug
import org.inkstack.parsers.model.MangaSource

@AndroidEntryPoint
class BrowserActivity : BaseBrowserActivity() {

	override fun onCreate2(savedInstanceState: Bundle?, source: MangaSource, repository: ParserMangaRepository?) {
		showCloseNavigation = true
		webView.webViewClient = BrowserClient(this)
		lifecycleScope.launch {
			try {
				proxyProvider.applyWebViewConfig()
			} catch (e: Exception) {
				e.printStackTraceDebug()
				snackbarHostState.showSnackbar(e.getDisplayMessage(resources))
			}
			if (savedInstanceState == null) {
				val url = intent?.dataString
				if (url.isNullOrEmpty()) {
					finishAfterTransition()
				} else {
					onTitleChanged(
						intent?.getStringExtra(AppRouter.KEY_TITLE) ?: getString(R.string.loading_),
						url,
					)
					webView.loadUrl(url)
				}
			}
		}
	}

	@Composable
	override fun RowScope.BrowserTopBarActions() {
		val openLabel = stringResource(R.string.open_in_browser)
		IconButton(
			onClick = {
				if (!router.openExternalBrowser(webView.url.orEmpty(), openLabel)) {
					lifecycleScope.launch {
						snackbarHostState.showSnackbar(getString(R.string.operation_not_supported))
					}
				}
			},
		) {
			Icon(
				painter = painterResource(R.drawable.ic_open_external),
				contentDescription = openLabel,
			)
		}
	}

	class Contract : ActivityResultContract<InteractiveActionRequiredException, Unit>() {
		override fun createIntent(
			context: Context,
			input: InteractiveActionRequiredException
		): Intent = AppRouter.browserIntent(
			context = context,
			url = input.url,
			source = input.source,
			title = null,
		)

		override fun parseResult(resultCode: Int, intent: Intent?): Unit = Unit
	}

	companion object {

		const val TAG = "BrowserActivity"
	}
}
