package org.inkstack.sync.ui

import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.app.Activity
import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.R
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.core.util.ext.getParcelableExtraCompat
import org.inkstack.sync.ui.compose.SyncAuthScreen

@AndroidEntryPoint
class SyncAuthActivity : BaseComposeActivity() {

	private val viewModel by viewModels<SyncAuthViewModel>()
	private var accountAuthenticatorResponse: AccountAuthenticatorResponse? = null
	private var resultBundle: Bundle? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		accountAuthenticatorResponse =
			intent.getParcelableExtraCompat(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
		accountAuthenticatorResponse?.onRequestContinued()
		setComposeContent {
			SyncAuthScreen(
				onCancel = {
					setResult(Activity.RESULT_CANCELED)
					finish()
				},
				onSuccess = { bundle ->
					resultBundle = bundle
					setResult(Activity.RESULT_OK)
					finish()
				},
				onAccountExists = {
					accountAuthenticatorResponse?.onError(
						AccountManager.ERROR_CODE_UNSUPPORTED_OPERATION,
						getString(R.string.account_already_exists),
					)
					finishAfterTransition()
				},
				viewModel = viewModel,
			)
		}
	}

	override fun finish() {
		accountAuthenticatorResponse?.let { response ->
			resultBundle?.also { response.onResult(it) }
				?: response.onError(AccountManager.ERROR_CODE_CANCELED, getString(R.string.canceled))
		}
		super.finish()
	}
}
