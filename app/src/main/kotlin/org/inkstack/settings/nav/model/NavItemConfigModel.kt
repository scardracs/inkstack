package org.inkstack.settings.nav.model

import androidx.annotation.StringRes
import org.inkstack.core.prefs.NavItem
import org.inkstack.list.ui.model.ListModel

data class NavItemConfigModel(
	val item: NavItem,
	@StringRes val disabledHintResId: Int,
) : ListModel {

	override fun areItemsTheSame(other: ListModel): Boolean {
		return other is NavItemConfigModel && other.item == item
	}
}
