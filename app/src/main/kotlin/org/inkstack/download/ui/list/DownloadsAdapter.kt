package org.inkstack.download.ui.list

import androidx.lifecycle.LifecycleOwner
import org.inkstack.core.ui.BaseListAdapter
import org.inkstack.list.ui.adapter.ListItemType
import org.inkstack.list.ui.adapter.emptyStateListAD
import org.inkstack.list.ui.adapter.listHeaderAD
import org.inkstack.list.ui.adapter.loadingStateAD
import org.inkstack.list.ui.model.ListModel

class DownloadsAdapter(
	lifecycleOwner: LifecycleOwner,
	listener: DownloadItemListener,
) : BaseListAdapter<ListModel>() {

	init {
		addDelegate(ListItemType.DOWNLOAD, downloadItemAD(lifecycleOwner, listener))
		addDelegate(ListItemType.STATE_LOADING, loadingStateAD())
		addDelegate(ListItemType.STATE_EMPTY, emptyStateListAD(null))
		addDelegate(ListItemType.HEADER, listHeaderAD(null))
	}
}
