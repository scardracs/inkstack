package org.inkstack.widget.recent

import android.app.PendingIntent
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

class RecentWidgetProvider : BaseAppWidgetProvider() {

	override fun onUpdateWidget(context: Context, config: AppWidgetConfig): RemoteViews {
		val views = RemoteViews(context.packageName, R.layout.widget_recent)
		if (!config.hasBackground) {
			views.setInt(R.id.widget_root, "setBackgroundColor", Color.TRANSPARENT)
		} else {
			views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.bg_appwidget_root)
		}
		val deps = EntryPointAccessors.fromApplication(context, WidgetDependencies::class.java)
		views.setRemoteAdapter(
			R.id.stackView,
			RecentWidgetContent.build(
				context = context,
				historyRepository = deps.historyRepository(),
				coilLazy = deps.imageLoader(),
				settings = deps.appSettings(),
			),
		)
		val intent = Intent(context, ReaderActivity::class.java)
		intent.action = ReaderIntent.ACTION_MANGA_READ
		views.setPendingIntentTemplate(
			R.id.stackView,
			PendingIntentCompat.getActivity(
				context,
				0,
				intent,
				PendingIntent.FLAG_UPDATE_CURRENT,
				true,
			),
		)
		views.setEmptyView(R.id.stackView, R.id.textView_holder)
		return views
	}
}
