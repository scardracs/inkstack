package org.inkstack.browser

import android.webkit.WebChromeClient
import android.webkit.WebView

class ProgressChromeClient(
	private val onProgressChanged: (progress: Int) -> Unit,
) : WebChromeClient() {

	override fun onProgressChanged(view: WebView?, newProgress: Int) {
		super.onProgressChanged(view, newProgress)
		onProgressChanged(newProgress)
	}
}
