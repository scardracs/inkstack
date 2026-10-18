package org.inkstack.settings.tracker.categories

import org.inkstack.core.model.FavouriteCategory
import org.inkstack.core.ui.BaseListAdapter
import org.inkstack.core.ui.list.OnListItemClickListener

class TrackerCategoriesConfigAdapter(
	listener: OnListItemClickListener<FavouriteCategory>,
) : BaseListAdapter<FavouriteCategory>() {

	init {
		delegatesManager.addDelegate(trackerCategoryAD(listener))
	}
}
