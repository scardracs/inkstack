package org.inkstack.favourites.ui.categories

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.inkstack.core.model.FavouriteCategory
import org.inkstack.core.ui.list.OnListItemClickListener

interface FavouriteCategoriesListListener : OnListItemClickListener<FavouriteCategory?> {

	fun onDragHandleTouch(holder: RecyclerView.ViewHolder): Boolean

	fun onEditClick(item: FavouriteCategory, view: View)

	fun onShowAllClick(isChecked: Boolean)
}
