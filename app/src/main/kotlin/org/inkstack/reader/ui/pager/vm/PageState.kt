package org.inkstack.reader.ui.pager.vm

import android.graphics.Rect
import android.net.Uri

sealed class PageState {

	data object Empty : PageState()

	data class Loading(
		val preview: Any?,
		val progress: Int,
	) : PageState()

	data class Loaded(
		val uri: Uri,
		val crop: Rect?,
		val isConverted: Boolean,
	) : PageState()

	class Converting() : PageState()

	data class Shown(
		val uri: Uri,
		val crop: Rect?,
		val isConverted: Boolean,
	) : PageState()

	data class Error(
		val error: Throwable,
	) : PageState()

	fun isFinalState(): Boolean = this is Error || this is Shown
}
