package org.inkstack.core.exceptions

class SyncApiException(
	message: String,
	val code: Int,
) : RuntimeException(message)
