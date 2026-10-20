package org.inkstack.scrobbling.common.ui.config

import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.core.nav.router
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.scrobbling.common.ui.config.compose.ScrobblerConfigScreen

@AndroidEntryPoint
class ScrobblerConfigActivity : BaseComposeActivity() {

	private var authCode by mutableStateOf<String?>(null)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		authCode = intent.takeAuthCode()
		setComposeContent {
			ScrobblerConfigScreen(
				onBack = { dispatchNavigateUp() },
				onOpenManga = { router.openDetails(it) },
				onLoggedOut = { finishAfterTransition() },
				onAuthCode = { /* handled via authCode state */ },
				authCode = authCode,
			)
		}
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		setIntent(intent)
		authCode = intent.takeAuthCode()
	}

	private fun Intent.takeAuthCode(): String? {
		if (action != Intent.ACTION_VIEW) return null
		return data?.getQueryParameter("code")?.takeIf { it.isNotEmpty() }
	}

	companion object {
		const val HOST_SHIKIMORI_AUTH = "shikimori-auth"
		const val HOST_ANILIST_AUTH = "anilist-auth"
		const val HOST_MAL_AUTH = "mal-auth"
		const val HOST_KITSU_AUTH = "kitsu-auth"
	}
}
