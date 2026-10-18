package org.inkstack.explore.ui.adapter

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.inkstack.R
import org.inkstack.core.model.getSummary
import org.inkstack.core.model.getTitle
import org.inkstack.core.ui.BaseListAdapter
import org.inkstack.core.ui.list.AdapterDelegateClickListenerAdapter
import org.inkstack.core.ui.list.OnListItemClickListener
import org.inkstack.core.util.ext.drawableStart
import org.inkstack.core.util.ext.recyclerView
import org.inkstack.core.util.ext.setProgressIcon
import org.inkstack.core.util.ext.setTooltipCompat
import org.inkstack.core.util.ext.textAndVisible
import org.inkstack.databinding.ItemExploreButtonsBinding
import org.inkstack.databinding.ItemExploreSourceGridBinding
import org.inkstack.databinding.ItemExploreSourceListBinding
import org.inkstack.databinding.ItemRecommendationBinding
import org.inkstack.databinding.ItemRecommendationMangaBinding
import org.inkstack.explore.ui.model.ExploreButtons
import org.inkstack.explore.ui.model.MangaSourceItem
import org.inkstack.explore.ui.model.RecommendationsItem
import org.inkstack.list.ui.adapter.ListItemType
import org.inkstack.list.ui.model.ListModel
import org.inkstack.list.ui.model.MangaCompactListModel
import org.inkstack.parsers.model.Manga
import org.inkstack.sourcescore.ui.HotSourceMarker

fun exploreButtonsAD(
	clickListener: View.OnClickListener,
) = adapterDelegateViewBinding<ExploreButtons, ListModel, ItemExploreButtonsBinding>(
	{ layoutInflater, parent -> ItemExploreButtonsBinding.inflate(layoutInflater, parent, false) },
) {

	binding.buttonBookmarks.setOnClickListener(clickListener)
	binding.buttonDownloads.setOnClickListener(clickListener)
	binding.buttonLocal.setOnClickListener(clickListener)
	binding.buttonRandom.setOnClickListener(clickListener)
	binding.buttonPresets.setOnClickListener(clickListener)
	binding.buttonPresetsDropdown.setOnClickListener(clickListener)

	bind {
		if (item.isRandomLoading) {
			binding.buttonRandom.setProgressIcon()
		} else {
			binding.buttonRandom.setIconResource(R.drawable.ic_dice)
		}
		binding.buttonRandom.isClickable = !item.isRandomLoading
		val presetName = item.activePresetName
		if (presetName != null) {
			binding.buttonPresets.text = presetName
		} else {
			binding.buttonPresets.setText(R.string.source_presets)
		}
	}
}

fun exploreRecommendationItemAD(
	itemClickListener: OnListItemClickListener<Manga>,
) = adapterDelegateViewBinding<RecommendationsItem, ListModel, ItemRecommendationBinding>(
	{ layoutInflater, parent -> ItemRecommendationBinding.inflate(layoutInflater, parent, false) },
) {

	val adapter = BaseListAdapter<MangaCompactListModel>()
		.addDelegate(ListItemType.MANGA_LIST, recommendationMangaItemAD(itemClickListener))
	binding.pager.adapter = adapter
	binding.pager.recyclerView?.isNestedScrollingEnabled = false
	binding.dots.bindToViewPager(binding.pager)

	bind {
		adapter.items = item.manga
	}
}

fun recommendationMangaItemAD(
	itemClickListener: OnListItemClickListener<Manga>,
) = adapterDelegateViewBinding<MangaCompactListModel, MangaCompactListModel, ItemRecommendationMangaBinding>(
	{ layoutInflater, parent -> ItemRecommendationMangaBinding.inflate(layoutInflater, parent, false) },
) {

	binding.root.setOnClickListener { v ->
		itemClickListener.onItemClick(item.manga, v)
	}
	bind {
		binding.textViewTitle.text = item.manga.title
		binding.textViewSubtitle.textAndVisible = item.subtitle
		binding.imageViewCover.setImageAsync(item.manga.coverUrl, item.manga.source)
	}
}


fun exploreSourceListItemAD(
	listener: OnListItemClickListener<MangaSourceItem>,
) = adapterDelegateViewBinding<MangaSourceItem, ListModel, ItemExploreSourceListBinding>(
	{ layoutInflater, parent ->
		ItemExploreSourceListBinding.inflate(
			layoutInflater,
			parent,
			false,
		)
	},
	on = { item, _, _ -> item is MangaSourceItem && !item.isGrid },
) {

	AdapterDelegateClickListenerAdapter(this, listener).attach(itemView)
	val iconPinned = ContextCompat.getDrawable(context, R.drawable.ic_pin_small)
	val hotMarker = HotSourceMarker(context)

	bind {
		binding.textViewTitle.text = hotMarker.decorate(item.source.getTitle(context), item.isTrending, atStart = true)
		binding.textViewTitle.drawableStart = if (item.source.isPinned) iconPinned else null
		binding.textViewSubtitle.text = item.source.getSummary(context)
		binding.imageViewIcon.setImageAsync(item.source)
	}
}

fun exploreSourceGridItemAD(
	listener: OnListItemClickListener<MangaSourceItem>,
) = adapterDelegateViewBinding<MangaSourceItem, ListModel, ItemExploreSourceGridBinding>(
	{ layoutInflater, parent ->
		ItemExploreSourceGridBinding.inflate(
			layoutInflater,
			parent,
			false,
		)
	},
	on = { item, _, _ -> item is MangaSourceItem && item.isGrid },
) {

	AdapterDelegateClickListenerAdapter(this, listener).attach(itemView)
	val iconPinned = ContextCompat.getDrawable(context, R.drawable.ic_pin_small)
	val hotMarker = HotSourceMarker(context)

	bind {
		val title = item.source.getTitle(context)
		itemView.setTooltipCompat(
			buildSpannedString {
				bold {
					append(title)
				}
				appendLine()
				append(item.source.getSummary(context))
			},
		)
		binding.textViewTitle.text = hotMarker.decorate(title, item.isTrending, atStart = true)
		binding.textViewTitle.drawableStart = if (item.source.isPinned) iconPinned else null
		binding.imageViewIcon.setImageAsync(item.source)
	}
}
