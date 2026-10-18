package org.inkstack.main.ui

import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.inkstack.R
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.prefs.observeAsFlow

/**
 * Double-back-to-exit for the legacy View-based MainActivity shell.
 * The Compose [org.inkstack.main.ui.compose.MainScreen] handles exit confirmation itself.
 */
class ExitCallback(
	private val activity: MainActivity,
	private val snackbarHost: android.view.View,
) : OnBackPressedCallback(false) {

	private var job: Job? = null
	private val isSearchOpen = MutableStateFlow(false)
	private val isDisabledByTimeout = MutableStateFlow(false)

	init {
		activity.lifecycleScope.launch {
			combine(
				observeSettings(),
				isSearchOpen,
				isDisabledByTimeout,
			) { enabledInSettings, searchOpen, disabledTemporary ->
				enabledInSettings && !searchOpen && !disabledTemporary
			}.collect {
				isEnabled = it
			}
		}
	}

	override fun handleOnBackPressed() {
		job?.cancel()
		job = activity.lifecycleScope.launch {
			resetExitConfirmation()
		}
	}

	fun setSearchOpen(isOpen: Boolean) {
		isSearchOpen.value = isOpen
	}

	private suspend fun resetExitConfirmation() {
		isDisabledByTimeout.value = true
		val snackbar = Snackbar.make(snackbarHost, R.string.confirm_exit, Snackbar.LENGTH_INDEFINITE)
		snackbar.show()
		delay(2000)
		snackbar.dismiss()
		isDisabledByTimeout.value = false
	}

	private fun observeSettings(): Flow<Boolean> = activity.settings
		.observeAsFlow(AppSettings.KEY_EXIT_CONFIRM) { isExitConfirmationEnabled }
		.flowOn(Dispatchers.Default)
}
