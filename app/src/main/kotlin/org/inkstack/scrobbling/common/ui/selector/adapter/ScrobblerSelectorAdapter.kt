package org.inkstack.scrobbling.common.ui.selector.adapter

import org.inkstack.core.ui.BaseListAdapter
import org.inkstack.core.ui.list.OnListItemClickListener
import org.inkstack.list.ui.adapter.ListItemType
import org.inkstack.list.ui.adapter.ListStateHolderListener
import org.inkstack.list.ui.adapter.loadingFooterAD
import org.inkstack.list.ui.adapter.loadingStateAD
import org.inkstack.list.ui.model.ListModel
import org.inkstack.scrobbling.common.domain.model.ScrobblerManga

class ScrobblerSelectorAdapter(
	clickListener: OnListItemClickListener<ScrobblerManga>,
	stateHolderListener: ListStateHolderListener,
) : BaseListAdapter<ListModel>() {

	init {
		addDelegate(ListItemType.STATE_LOADING, loadingStateAD())
		addDelegate(ListItemType.MANGA_SCROBBLING, scrobblingMangaAD(clickListener))
		addDelegate(ListItemType.FOOTER_LOADING, loadingFooterAD())
		addDelegate(ListItemType.HINT_EMPTY, scrobblerHintAD(stateHolderListener))
	}
}
