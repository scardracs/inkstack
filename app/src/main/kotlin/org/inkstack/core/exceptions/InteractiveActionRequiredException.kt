package org.inkstack.core.exceptions

import okio.IOException
import org.inkstack.parsers.model.MangaSource

class InteractiveActionRequiredException(
	val source: MangaSource,
	val url: String,
) : IOException("Interactive action is required for ${source.name}")
