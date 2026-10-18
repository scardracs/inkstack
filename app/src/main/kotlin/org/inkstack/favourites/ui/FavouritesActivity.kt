package org.inkstack.favourites.ui

import android.os.Bundle
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.ui.FragmentContainerActivity
import org.inkstack.favourites.ui.list.FavouritesListFragment

class FavouritesActivity : FragmentContainerActivity(FavouritesListFragment::class.java) {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val categoryTitle = intent.getStringExtra(AppRouter.KEY_TITLE)
		if (categoryTitle != null) {
			title = categoryTitle
		}
	}
}
