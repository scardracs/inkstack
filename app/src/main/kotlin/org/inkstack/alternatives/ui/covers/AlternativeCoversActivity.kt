package org.inkstack.alternatives.ui.covers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.alternatives.ui.covers.compose.AlternativeCoversScreen
import org.inkstack.core.model.parcelable.ParcelableManga
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.parsers.model.Manga

@AndroidEntryPoint
class AlternativeCoversActivity : BaseComposeActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setComposeContent {
			AlternativeCoversScreen(
				onBack = { finishAfterTransition() },
				onApply = { url ->
					setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_COVER_URL, url))
					finish()
				},
			)
		}
	}

	class Contract : ActivityResultContract<Manga, String?>() {

		override fun createIntent(context: Context, input: Manga) =
			Intent(context, AlternativeCoversActivity::class.java)
				.putExtra(AppRouter.KEY_MANGA, ParcelableManga(input, withDescription = false))

		override fun parseResult(resultCode: Int, intent: Intent?): String? = if (resultCode == Activity.RESULT_OK) {
			intent?.getStringExtra(EXTRA_COVER_URL)
		} else {
			null
		}
	}

	private companion object {
		const val EXTRA_COVER_URL = "cover_url"
	}
}
