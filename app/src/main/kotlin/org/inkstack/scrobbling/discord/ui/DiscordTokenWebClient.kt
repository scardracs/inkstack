package org.inkstack.scrobbling.discord.ui

import android.graphics.Bitmap
import android.webkit.WebView
import org.inkstack.browser.BrowserCallback
import org.inkstack.browser.BrowserClient
import org.inkstack.parsers.util.removeSurrounding

class DiscordTokenWebClient(private val callback: Callback) : BrowserClient(callback) {

	override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
		super.onPageStarted(view, url, favicon)
		if (view != null) {
			checkToken(view)
		}
	}

	private fun checkToken(view: WebView) {
		view.evaluateJavascript("window.localStorage.token") { result ->
			val token = result
				?.replace("\\\"", "")
				?.removeSurrounding('"')
				?.takeUnless { it == "null" }
			if (!token.isNullOrEmpty()) {
				callback.onTokenObtained(token)
			}
		}
	}

	interface Callback : BrowserCallback {

		fun onTokenObtained(token: String)
	}
}
