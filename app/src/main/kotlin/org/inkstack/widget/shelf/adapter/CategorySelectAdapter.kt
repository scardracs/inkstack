package org.inkstack.widget.shelf.adapter

import org.inkstack.core.ui.BaseListAdapter
import org.inkstack.core.ui.list.OnListItemClickListener
import org.inkstack.widget.shelf.model.CategoryItem

class CategorySelectAdapter(
	clickListener: OnListItemClickListener<CategoryItem>
) : BaseListAdapter<CategoryItem>() {

	init {
		delegatesManager.addDelegate(categorySelectItemAD(clickListener))
	}
}
