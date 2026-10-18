package org.inkstack.parsers.exception

import okio.IOException
import org.inkstack.parsers.InternalParsersApi
import org.inkstack.parsers.model.MangaSource

/**
 * Authorization is required for access to the requested content
 */
public class AuthRequiredException @InternalParsersApi @JvmOverloads constructor(
	public val source: MangaSource,
	cause: Throwable? = null,
) : IOException("Authorization required", cause)
