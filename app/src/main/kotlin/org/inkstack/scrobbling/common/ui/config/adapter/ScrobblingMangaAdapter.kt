package org.inkstack.scrobbling.common.ui.config.adapter

import org.inkstack.core.ui.BaseListAdapter
import org.inkstack.core.ui.list.OnListItemClickListener
import org.inkstack.list.ui.adapter.ListItemType
import org.inkstack.list.ui.adapter.emptyStateListAD
import org.inkstack.list.ui.model.ListModel
import org.inkstack.scrobbling.common.domain.model.ScrobblingInfo

class ScrobblingMangaAdapter(
	clickListener: OnListItemClickListener<ScrobblingInfo>,
) : BaseListAdapter<ListModel>() {

	init {
		addDelegate(ListItemType.HEADER, scrobblingHeaderAD())
		addDelegate(ListItemType.STATE_EMPTY, emptyStateListAD(null))
		addDelegate(ListItemType.MANGA_SCROBBLING, scrobblingMangaAD(clickListener))
	}
}
