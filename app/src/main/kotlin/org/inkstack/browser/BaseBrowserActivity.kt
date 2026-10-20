package org.inkstack.browser

import android.os.Bundle
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.R
import org.inkstack.browser.compose.BrowserScreen
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.network.CommonHeaders
import org.inkstack.core.network.proxy.ProxyProvider
import org.inkstack.core.parser.MangaRepository
import org.inkstack.core.parser.ParserMangaRepository
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.core.util.ext.configureForParser
import org.inkstack.core.util.ext.isWebViewUnavailable
import org.inkstack.core.model.MangaSource
import org.inkstack.parsers.model.MangaSource
import org.inkstack.parsers.util.nullIfEmpty
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseBrowserActivity : BaseComposeActivity(), BrowserCallback {

	@Inject
	lateinit var proxyProvider: ProxyProvider

	@Inject
	lateinit var mangaRepositoryFactory: MangaRepository.Factory

	protected lateinit var webView: WebView
		private set

	private lateinit var onBackPressedCallback: WebViewBackPressedCallback

	protected val snackbarHostState = SnackbarHostState()

	protected var browserTitle by mutableStateOf<CharSequence>("")
		private set

	protected var browserSubtitle by mutableStateOf<CharSequence?>(null)
		private set

	protected var isLoading by mutableStateOf(false)
		private set

	protected var loadProgress by mutableIntStateOf(0)
		private set

	protected var showChrome by mutableStateOf(true)

	/**
	 * Applied to the [WebView] via [android.view.View.setAlpha] (not Compose graphicsLayer) so
	 * Cloudflare Turnstile still sees a rendered, focused surface in hidden auto-resolve mode.
	 */
	protected var contentAlpha: Float = 1f
		set(value) {
			field = value
			if (::webView.isInitialized) {
				webView.alpha = value
			}
		}

	protected var showCloseNavigation by mutableStateOf(false)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val createdWebView = try {
			WebView(this)
		} catch (e: Exception) {
			if (e.isWebViewUnavailable()) {
				Toast.makeText(this, R.string.web_view_unavailable, Toast.LENGTH_LONG).show()
				finishAfterTransition()
				return
			}
			throw e
		}
		webView = createdWebView
		webView.webChromeClient = ProgressChromeClient { progress ->
			if (isLoading) {
				loadProgress = progress
			}
		}
		onBackPressedCallback = WebViewBackPressedCallback(webView)
		onBackPressedDispatcher.addCallback(onBackPressedCallback)

		val mangaSource = MangaSource(intent?.getStringExtra(AppRouter.KEY_SOURCE))
		val repository = mangaRepositoryFactory.create(mangaSource) as? ParserMangaRepository
		val userAgent = intent?.getStringExtra(AppRouter.KEY_USER_AGENT)?.nullIfEmpty()
			?: repository?.getRequestHeaders()?.get(CommonHeaders.USER_AGENT)
		webView.configureForParser(userAgent)

		// Attach the WebView via Compose before subclass setup so CF viewport waits can observe layout.
		setComposeContent {
			BrowserScreen(
				webView = webView,
				title = browserTitle,
				subtitle = browserSubtitle,
				isLoading = isLoading,
				loadProgress = loadProgress,
				showChrome = showChrome,
				showCloseNavigation = showCloseNavigation,
				snackbarHostState = snackbarHostState,
				onClose = ::onNavigationClose,
				actions = { BrowserTopBarActions() },
			)
		}
		onCreate2(savedInstanceState, mangaSource, repository)
	}

	protected abstract fun onCreate2(
		savedInstanceState: Bundle?,
		source: MangaSource,
		repository: ParserMangaRepository?
	)

	@Composable
	protected open fun RowScope.BrowserTopBarActions() = Unit

	protected open fun onNavigationClose() {
		webView.stopLoading()
		finishAfterTransition()
	}

	override fun onPause() {
		if (::webView.isInitialized) {
			webView.onPause()
		}
		super.onPause()
	}

	override fun onResume() {
		super.onResume()
		if (::webView.isInitialized) {
			webView.onResume()
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		if (::webView.isInitialized) {
			webView.stopLoading()
			webView.destroy()
		}
	}

	override fun onLoadingStateChanged(isLoading: Boolean) {
		this.isLoading = isLoading
		if (isLoading) {
			loadProgress = 0
		}
	}

	override fun onTitleChanged(title: CharSequence, subtitle: CharSequence?) {
		this.title = title
		browserTitle = title
		browserSubtitle = subtitle
	}

	override fun onHistoryChanged() {
		onBackPressedCallback.onHistoryChanged()
	}

	protected fun setBrowserLoadingVisible(visible: Boolean) {
		isLoading = visible
	}
}
