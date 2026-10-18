package org.inkstack.core.exceptions

import okhttp3.Headers
import org.inkstack.core.model.UnknownMangaSource
import org.inkstack.parsers.model.MangaSource
import org.inkstack.parsers.network.CloudFlareHelper

class CloudFlareProtectedException(
	override val url: String,
	source: MangaSource?,
	@Transient val headers: Headers,
) : CloudFlareException("Protected by CloudFlare", CloudFlareHelper.PROTECTION_CAPTCHA) {

	override val source: MangaSource = source ?: UnknownMangaSource
}
