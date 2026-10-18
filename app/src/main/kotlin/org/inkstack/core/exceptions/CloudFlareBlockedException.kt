package org.inkstack.core.exceptions

import org.inkstack.core.model.UnknownMangaSource
import org.inkstack.parsers.model.MangaSource
import org.inkstack.parsers.network.CloudFlareHelper

class CloudFlareBlockedException(
	override val url: String,
	source: MangaSource?,
) : CloudFlareException("Blocked by CloudFlare", CloudFlareHelper.PROTECTION_BLOCKED) {

	override val source: MangaSource = source ?: UnknownMangaSource
}
