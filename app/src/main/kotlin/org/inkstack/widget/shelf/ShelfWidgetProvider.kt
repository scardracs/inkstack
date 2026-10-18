package org.inkstack.widget.shelf

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import androidx.core.app.PendingIntentCompat
import dagger.hilt.android.EntryPointAccessors
import org.inkstack.R
import org.inkstack.core.nav.ReaderIntent
import org.inkstack.core.prefs.AppWidgetConfig
import org.inkstack.core.ui.BaseAppWidgetProvider
import org.inkstack.reader.ui.ReaderActivity
import org.inkstack.widget.WidgetDependencies

class ShelfWidgetProvider : BaseAppWidgetProvider() {

	override fun onUpdateWidget(context: Context, config: AppWidgetConfig): RemoteViews {
		val views = RemoteViews(context.packageName, R.layout.widget_shelf)
		if (!config.hasBackground) {
			views.setInt(R.id.widget_root, "setBackgroundColor", Color.TRANSPARENT)
		} else {
			views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.bg_appwidget_root)
		}
		val deps = EntryPointAccessors.fromApplication(context, WidgetDependencies::class.java)
		views.setRemoteAdapter(
			R.id.gridView,
			ShelfWidgetContent.build(
				context = context,
				config = config,
				favouritesRepository = deps.favouritesRepository(),
				coilLazy = deps.imageLoader(),
				settings = deps.appSettings(),
			),
		)
		val intent = Intent(context, ReaderActivity::class.java)
		intent.action = ReaderIntent.ACTION_MANGA_READ
		views.setPendingIntentTemplate(
			R.id.gridView,
			PendingIntentCompat.getActivity(
				context,
				0,
				intent,
				PendingIntent.FLAG_UPDATE_CURRENT,
				true,
			),
		)
		views.setEmptyView(R.id.gridView, R.id.textView_holder)
		return views
	}
}
