package org.inkstack.details.ui.pager.pages

import android.content.Context
import org.inkstack.core.ui.BaseListAdapter
import org.inkstack.core.ui.list.OnListItemClickListener
import org.inkstack.core.ui.list.fastscroll.FastScroller
import org.inkstack.list.ui.adapter.ListItemType
import org.inkstack.list.ui.adapter.listHeaderAD
import org.inkstack.list.ui.model.ListModel

class PageThumbnailAdapter(
	clickListener: OnListItemClickListener<PageThumbnail>,
) : BaseListAdapter<ListModel>(), FastScroller.SectionIndexer {

	init {
		addDelegate(ListItemType.PAGE_THUMB, pageThumbnailAD(clickListener))
		addDelegate(ListItemType.HEADER, listHeaderAD(null))
	}

	override fun getSectionText(context: Context, position: Int): CharSequence? {
		return findHeader(position)?.getText(context)
	}
}
