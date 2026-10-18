package org.inkstack.bookmarks.ui.adapter

import android.content.Context
import org.inkstack.bookmarks.domain.Bookmark
import org.inkstack.core.ui.BaseListAdapter
import org.inkstack.core.ui.list.OnListItemClickListener
import org.inkstack.core.ui.list.fastscroll.FastScroller
import org.inkstack.list.ui.adapter.ListHeaderClickListener
import org.inkstack.list.ui.adapter.ListItemType
import org.inkstack.list.ui.adapter.emptyStateListAD
import org.inkstack.list.ui.adapter.errorStateListAD
import org.inkstack.list.ui.adapter.listHeaderAD
import org.inkstack.list.ui.adapter.loadingFooterAD
import org.inkstack.list.ui.adapter.loadingStateAD
import org.inkstack.list.ui.model.ListModel

class BookmarksAdapter(
	clickListener: OnListItemClickListener<Bookmark>,
	headerClickListener: ListHeaderClickListener?,
) : BaseListAdapter<ListModel>(), FastScroller.SectionIndexer {

	init {
		addDelegate(ListItemType.PAGE_THUMB, bookmarkLargeAD(clickListener))
		addDelegate(ListItemType.HEADER, listHeaderAD(headerClickListener))
		addDelegate(ListItemType.STATE_ERROR, errorStateListAD(null))
		addDelegate(ListItemType.FOOTER_LOADING, loadingFooterAD())
		addDelegate(ListItemType.STATE_LOADING, loadingStateAD())
		addDelegate(ListItemType.STATE_EMPTY, emptyStateListAD(null))
	}

	override fun getSectionText(context: Context, position: Int): CharSequence? {
		return findHeader(position)?.getText(context)
	}
}
