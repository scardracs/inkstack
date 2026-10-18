package org.inkstack.scrobbling.common.domain

import okio.IOException
import org.inkstack.scrobbling.common.domain.model.ScrobblerService

class ScrobblerAuthRequiredException(
	val scrobbler: ScrobblerService,
) : IOException()
