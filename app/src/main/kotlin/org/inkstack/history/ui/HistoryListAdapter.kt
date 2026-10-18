package org.inkstack.history.ui

import android.content.Context
import org.inkstack.core.ui.list.fastscroll.FastScroller
import org.inkstack.list.ui.adapter.MangaListAdapter
import org.inkstack.list.ui.adapter.MangaListListener
import org.inkstack.list.ui.size.ItemSizeResolver

class HistoryListAdapter(
	listener: MangaListListener,
	sizeResolver: ItemSizeResolver,
) : MangaListAdapter(listener, sizeResolver), FastScroller.SectionIndexer {

	override fun getSectionText(context: Context, position: Int): CharSequence? {
		return findHeader(position)?.getText(context)
	}
}
