package org.inkstack.explore.ui

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import org.inkstack.R
import org.inkstack.core.nav.AppRouter
import org.inkstack.explore.ui.preset.SourcePresetListActivity

class ExploreMenuProvider(
	private val context: Context,
	private val router: AppRouter,
) : MenuProvider {

	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.opt_explore, menu)
	}

	override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
		return when (menuItem.itemId) {
			R.id.action_manage -> {
				router.openSourcesSettings()
				true
			}

			R.id.action_presets -> {
				context.startActivity(android.content.Intent(context, SourcePresetListActivity::class.java))
				true
			}

			else -> false
		}
	}
}
