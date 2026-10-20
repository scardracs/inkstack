package org.inkstack.favourites.ui.categories

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.core.nav.router
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.favourites.ui.categories.compose.FavouriteCategoriesScreen

@AndroidEntryPoint
class FavouriteCategoriesActivity : BaseComposeActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setComposeContent {
			FavouriteCategoriesScreen(
				onBack = { dispatchNavigateUp() },
				onOpenAll = { router.openFavorites() },
				onOpenCategory = { router.openFavorites(it) },
				onEditCategory = { router.openFavoriteCategoryEdit(it) },
				onCreate = { router.openFavoriteCategoryCreate() },
			)
		}
	}
}
