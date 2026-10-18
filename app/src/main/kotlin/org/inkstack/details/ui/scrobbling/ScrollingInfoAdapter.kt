package org.inkstack.details.ui.scrobbling

import org.inkstack.core.nav.AppRouter
import org.inkstack.core.ui.BaseListAdapter
import org.inkstack.list.ui.model.ListModel

class ScrollingInfoAdapter(
	router: AppRouter,
) : BaseListAdapter<ListModel>() {

	init {
		delegatesManager.addDelegate(scrobblingInfoAD(router))
	}
}
