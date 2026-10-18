package org.inkstack.list.ui.size

import android.view.View
import android.widget.TextView
import org.inkstack.history.ui.util.ReadingProgressView

interface ItemSizeResolver {

	val cellWidth: Int

	fun attachToView(
		view: View,
		textView: TextView?,
		progressView: ReadingProgressView?,
	)
}
