package org.inkstack.core.ui.compose

import android.view.Menu
import android.view.MenuItem
import androidx.annotation.DrawableRes
import org.inkstack.R

fun Menu.toChromeActions(): List<SelectionChromeAction> = buildList {
	for (i in 0 until size()) {
		val item = getItem(i)
		val iconRes = selectionActionIconRes(item.itemId)
		add(
			SelectionChromeAction(
				id = item.itemId,
				title = item.title?.toString().orEmpty(),
				iconRes = iconRes,
				visible = item.isVisible,
				enabled = item.isEnabled,
				// mode_* menus mark quick actions as ifRoom — show those with icons inline.
				showInline = iconRes != 0,
			),
		)
	}
}

/**
 * Explicit id→drawable map. Do not reflect into AppCompat [MenuItemImpl]: Android 14+
 * blocks setAccessible on those internals, so release builds always saw iconRes=0 and
 * drew empty IconButtons (looked like “icons same color as the pill”).
 */
@DrawableRes
internal fun selectionActionIconRes(itemId: Int): Int = when (itemId) {
	R.id.action_share -> R.drawable.ic_share
	R.id.action_save -> R.drawable.ic_save
	R.id.action_select_all -> R.drawable.ic_select_all
	R.id.action_select_range -> R.drawable.ic_select_range
	R.id.action_remove, R.id.action_delete -> R.drawable.ic_delete
	R.id.action_favourite -> R.drawable.ic_heart_outline
	R.id.action_pin -> R.drawable.ic_pin
	R.id.action_unpin -> R.drawable.ic_unpin
	R.id.action_replace_source -> R.drawable.ic_replace
	R.id.action_edit_override -> R.drawable.ic_edit
	R.id.action_mark_current -> R.drawable.ic_eye_check
	R.id.action_resume -> R.drawable.ic_action_resume
	R.id.action_pause -> R.drawable.ic_action_pause
	R.id.action_cancel -> R.drawable.ic_cancel_multiple
	R.id.action_disable -> R.drawable.ic_eye_off
	R.id.action_shortcut -> R.drawable.ic_shortcut
	R.id.action_settings -> R.drawable.ic_settings
	R.id.action_show -> R.drawable.ic_eye
	R.id.action_hide -> R.drawable.ic_eye_off
	else -> 0
}
