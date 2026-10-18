package org.inkstack.core.image

import java.io.IOException

/**
 * Local replacement for the former SSIV [com.davemorrissey.labs.subscaleview.decoder.ImageDecodeException].
 */
class ImageDecodeException(
	val uri: String? = null,
	val format: String? = null,
	message: String? = null,
	cause: Throwable? = null,
) : IOException(message ?: "Image decode failed", cause)
