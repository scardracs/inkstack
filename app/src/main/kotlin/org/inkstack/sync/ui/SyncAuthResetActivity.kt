package org.inkstack.sync.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.R
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.sync.ui.compose.SyncAuthResetScreen

@AndroidEntryPoint
class SyncAuthResetActivity : BaseComposeActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val valid = intent.action == Intent.ACTION_VIEW && intent.data?.host == HOST_RESET_PASSWORD
		val baseUrl = intent.data?.getQueryParameter("base_url")?.trim()
		val token = intent.data?.getQueryParameter("token")?.trim()
		setComposeContent {
			SyncAuthResetScreen(
				baseUrl = baseUrl.takeIf { valid },
				token = token.takeIf { valid },
				onCancel = {
					setResult(Activity.RESULT_CANCELED)
					finish()
				},
				onSuccess = {
					setResult(Activity.RESULT_OK)
					finishAfterTransition()
				},
				onInvalid = {
					Toast.makeText(this, R.string.no_reset_token, Toast.LENGTH_SHORT).show()
					finishAfterTransition()
				},
			)
		}
	}

	companion object {
		private const val HOST_RESET_PASSWORD = "reset-password"
	}
}
