package org.inkstack.tracker.ui.feed.adapter

import android.content.Context
import org.inkstack.core.ui.BaseListAdapter
import org.inkstack.core.ui.list.OnListItemClickListener
import org.inkstack.core.ui.list.fastscroll.FastScroller
import org.inkstack.list.ui.adapter.ListItemType
import org.inkstack.list.ui.adapter.MangaListListener
import org.inkstack.list.ui.adapter.emptyStateListAD
import org.inkstack.list.ui.adapter.errorFooterAD
import org.inkstack.list.ui.adapter.errorStateListAD
import org.inkstack.list.ui.adapter.listHeaderAD
import org.inkstack.list.ui.adapter.loadingFooterAD
import org.inkstack.list.ui.adapter.loadingStateAD
import org.inkstack.list.ui.adapter.quickFilterAD
import org.inkstack.list.ui.model.ListModel
import org.inkstack.list.ui.size.ItemSizeResolver
import org.inkstack.tracker.ui.feed.model.FeedItem

class FeedAdapter(
	listener: MangaListListener,
	sizeResolver: ItemSizeResolver,
	feedClickListener: OnListItemClickListener<FeedItem>,
) : BaseListAdapter<ListModel>(), FastScroller.SectionIndexer {

	init {
		addDelegate(ListItemType.FEED, feedItemAD(feedClickListener))
		addDelegate(
			ListItemType.MANGA_NESTED_GROUP,
			updatedMangaAD(
				sizeResolver = sizeResolver,
				listener = listener,
				headerClickListener = listener,
			),
		)
		addDelegate(ListItemType.FOOTER_LOADING, loadingFooterAD())
		addDelegate(ListItemType.STATE_LOADING, loadingStateAD())
		addDelegate(ListItemType.FOOTER_ERROR, errorFooterAD(listener))
		addDelegate(ListItemType.STATE_ERROR, errorStateListAD(listener))
		addDelegate(ListItemType.HEADER, listHeaderAD(listener))
		addDelegate(ListItemType.STATE_EMPTY, emptyStateListAD(listener))
		addDelegate(ListItemType.QUICK_FILTER, quickFilterAD(listener))
	}

	override fun getSectionText(context: Context, position: Int): CharSequence? {
		return findHeader(position)?.getText(context)
	}
}
