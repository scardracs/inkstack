package org.inkstack.settings.sources.catalog

import android.content.Context
import org.inkstack.core.model.getTitle
import org.inkstack.core.ui.BaseListAdapter
import org.inkstack.core.ui.list.OnListItemClickListener
import org.inkstack.core.ui.list.fastscroll.FastScroller
import org.inkstack.list.ui.adapter.ListItemType
import org.inkstack.list.ui.adapter.loadingStateAD
import org.inkstack.list.ui.model.ListModel

class SourcesCatalogAdapter(
	listener: OnListItemClickListener<SourceCatalogItem.Source>,
) : BaseListAdapter<ListModel>(), FastScroller.SectionIndexer {

	init {
		addDelegate(ListItemType.CHAPTER_LIST, sourceCatalogItemSourceAD(listener))
		addDelegate(ListItemType.HINT_EMPTY, sourceCatalogItemHintAD())
		addDelegate(ListItemType.STATE_LOADING, loadingStateAD())
	}

	override fun getSectionText(context: Context, position: Int): CharSequence? {
		return (items.getOrNull(position) as? SourceCatalogItem.Source)?.source?.getTitle(context)?.take(1)
	}
}
