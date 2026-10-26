package org.inkstack.core.ui.compose

import android.view.Menu
import android.view.MenuItem

fun Menu.toChromeActions(): List<SelectionChromeAction> = buildList {
	for (i in 0 until size()) {
		val item = getItem(i)
		add(
			SelectionChromeAction(
				id = item.itemId,
				title = item.title?.toString().orEmpty(),
				iconRes = item.iconResId(),
				visible = item.isVisible,
				enabled = item.isEnabled,
				showInline = item.wantsInlineAction(),
			),
		)
	}
}

private fun MenuItem.iconResId(): Int {
	try {
		val viaMethod = javaClass.getMethod("getIconResId").invoke(this) as Int
		if (viaMethod != 0) return viaMethod
	} catch (_: ReflectiveOperationException) {
		// fall through
	}
	return try {
		val field = javaClass.getDeclaredField("mIconResId").apply { isAccessible = true }
		field.getInt(this)
	} catch (_: ReflectiveOperationException) {
		0
	} catch (_: IllegalAccessException) {
		0
	}
}

private fun MenuItem.wantsInlineAction(): Boolean {
	if (icon == null && iconResId() == 0) return false
	return try {
		val flags = javaClass.getMethod("getShowAsAction").invoke(this) as Int
		flags and (MenuItem.SHOW_AS_ACTION_IF_ROOM or MenuItem.SHOW_AS_ACTION_ALWAYS) != 0
	} catch (_: ReflectiveOperationException) {
		true
	}
}
