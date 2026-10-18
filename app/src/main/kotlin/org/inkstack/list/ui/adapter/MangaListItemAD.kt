package org.inkstack.list.ui.adapter

import androidx.core.view.isVisible
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.inkstack.core.ui.list.AdapterDelegateClickListenerAdapter
import org.inkstack.core.ui.list.OnListItemClickListener
import org.inkstack.core.util.ext.setTooltipCompat
import org.inkstack.core.util.ext.textAndVisible
import org.inkstack.databinding.ItemMangaListBinding
import org.inkstack.list.ui.model.ListModel
import org.inkstack.list.ui.model.MangaCompactListModel
import org.inkstack.list.ui.model.MangaListModel

fun mangaListItemAD(
	clickListener: OnListItemClickListener<MangaListModel>,
) = adapterDelegateViewBinding<MangaCompactListModel, ListModel, ItemMangaListBinding>(
	{ inflater, parent -> ItemMangaListBinding.inflate(inflater, parent, false) },
) {

	AdapterDelegateClickListenerAdapter(this, clickListener).attach(itemView)

	bind {
		itemView.setTooltipCompat(item.getSummary(context))
		binding.textViewTitle.text = item.title
		binding.textViewSubtitle.textAndVisible = item.subtitle
		binding.imageViewCover.setImageAsync(item.coverUrl, item.manga)
		binding.badge.number = item.counter
		binding.badge.isVisible = item.counter > 0
	}
}
