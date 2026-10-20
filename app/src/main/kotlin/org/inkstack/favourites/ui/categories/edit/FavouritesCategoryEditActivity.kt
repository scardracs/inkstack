package org.inkstack.favourites.ui.categories.edit

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.favourites.ui.categories.compose.FavouritesCategoryEditScreen

@AndroidEntryPoint
class FavouritesCategoryEditActivity : BaseComposeActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setComposeContent {
			FavouritesCategoryEditScreen(
				onBack = { dispatchNavigateUp() },
				onSaved = { finishAfterTransition() },
			)
		}
	}

	companion object {
		const val NO_ID = -1L
	}
}
