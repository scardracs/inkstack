package org.inkstack.favourites.ui.categories.adapter

import org.inkstack.core.ui.ReorderableListAdapter
import org.inkstack.favourites.ui.categories.FavouriteCategoriesListListener
import org.inkstack.list.ui.adapter.ListItemType
import org.inkstack.list.ui.adapter.ListStateHolderListener
import org.inkstack.list.ui.adapter.emptyStateListAD
import org.inkstack.list.ui.adapter.loadingStateAD
import org.inkstack.list.ui.model.ListModel

class CategoriesAdapter(
	onItemClickListener: FavouriteCategoriesListListener,
	listListener: ListStateHolderListener,
) : ReorderableListAdapter<ListModel>() {

	init {
		addDelegate(ListItemType.CATEGORY_LARGE, categoryAD(onItemClickListener))
		addDelegate(ListItemType.NAV_ITEM, allCategoriesAD(onItemClickListener))
		addDelegate(ListItemType.STATE_EMPTY, emptyStateListAD(listListener))
		addDelegate(ListItemType.STATE_LOADING, loadingStateAD())
	}
}
