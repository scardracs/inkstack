package org.inkstack.scrobbling.discord.ui

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.browser.BaseBrowserActivity
import org.inkstack.core.parser.ParserMangaRepository
import org.inkstack.core.prefs.AppSettings
import org.inkstack.parsers.model.MangaSource
import javax.inject.Inject

@AndroidEntryPoint
class DiscordAuthActivity : BaseBrowserActivity(), DiscordTokenWebClient.Callback {

	@Inject
	lateinit var settings: AppSettings

	override fun onCreate2(
		savedInstanceState: Bundle?,
		source: MangaSource,
		repository: ParserMangaRepository?
	) {
		showCloseNavigation = true
		webView.settings.userAgentString = USER_AGENT
		webView.webViewClient = DiscordTokenWebClient(this)
		if (savedInstanceState == null) {
			webView.loadUrl(BASE_URL)
		}
	}

	override fun onTokenObtained(token: String) {
		settings.discordToken = token
		setResult(RESULT_OK)
		finish()
	}

	private companion object {

		const val BASE_URL = "https://discord.com/login"
		private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14; SM-S921U; Build/UP1A.231005.007) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Mobile Safari/537.363"
	}
}
