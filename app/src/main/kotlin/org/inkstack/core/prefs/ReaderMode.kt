package org.inkstack.core.prefs

import androidx.annotation.Keep

@Keep
enum class ReaderMode(val id: Int) {

	WEBTOON(2),
	;

	companion object {

		fun valueOf(id: Int): ReaderMode = WEBTOON
	}
}
