package org.inkstack.widget.shelf

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.graphics.drawable.toBitmap
import coil3.ImageLoader
import coil3.executeBlocking
import coil3.request.ImageRequest
import coil3.request.transformations
import coil3.size.Size
import coil3.transform.RoundedCornersTransformation
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import org.inkstack.R
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.prefs.AppWidgetConfig
import org.inkstack.core.ui.image.TrimTransformation
import org.inkstack.core.util.ext.getDrawableOrThrow
import org.inkstack.core.util.ext.mangaExtra
import org.inkstack.favourites.domain.FavouritesRepository
import org.inkstack.parsers.model.Manga

internal object ShelfWidgetContent {

	fun build(
		context: Context,
		config: AppWidgetConfig,
		favouritesRepository: FavouritesRepository,
		coilLazy: Lazy<ImageLoader>,
		settings: AppSettings,
	): RemoteViews.RemoteCollectionItems {
		val data = if (settings.appPassword.isNullOrEmpty()) {
			runBlocking {
				val category = config.categoryId
				if (category == 0L) {
					favouritesRepository.getAllManga()
				} else {
					favouritesRepository.getManga(category)
				}
			}
		} else {
			emptyList()
		}
		val transformation = RoundedCornersTransformation(
			context.resources.getDimension(R.dimen.appwidget_corner_radius_inner),
		)
		val coverSize = Size(
			context.resources.getDimensionPixelSize(R.dimen.widget_cover_width),
			context.resources.getDimensionPixelSize(R.dimen.widget_cover_height),
		)
		val builder = RemoteViews.RemoteCollectionItems.Builder()
			.setHasStableIds(true)
			.setViewTypeCount(1)
		for (item in data) {
			builder.addItem(item.id, createItemView(context, item, coilLazy, transformation, coverSize))
		}
		return builder.build()
	}

	private fun createItemView(
		context: Context,
		item: Manga,
		coilLazy: Lazy<ImageLoader>,
		transformation: RoundedCornersTransformation,
		coverSize: Size,
	): RemoteViews {
		val views = RemoteViews(context.packageName, R.layout.item_shelf)
		views.setTextViewText(R.id.textView_title, item.title)
		runCatching {
			coilLazy.get().executeBlocking(
				ImageRequest.Builder(context)
					.data(item.coverUrl)
					.size(coverSize)
					.mangaExtra(item)
					.transformations(transformation, TrimTransformation())
					.build(),
			).getDrawableOrThrow().toBitmap()
		}.onSuccess { cover ->
			views.setImageViewBitmap(R.id.imageView_cover, cover)
		}.onFailure {
			views.setImageViewResource(R.id.imageView_cover, R.drawable.ic_placeholder)
		}
		val intent = Intent()
		intent.putExtra(AppRouter.KEY_ID, item.id)
		views.setOnClickFillInIntent(R.id.rootLayout, intent)
		return views
	}
}
