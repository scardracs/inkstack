package org.inkstack.settings.protect

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.ui.BaseViewModel
import org.inkstack.core.util.ext.MutableEventFlow
import org.inkstack.core.util.ext.call
import org.inkstack.parsers.util.isNumeric
import org.inkstack.parsers.util.md5
import javax.inject.Inject

@HiltViewModel
class ProtectSetupViewModel @Inject constructor(
	private val settings: AppSettings,
) : BaseViewModel() {

	private val firstPassword = MutableStateFlow<String?>(null)

	val isSecondStep = firstPassword.map {
		it != null
	}.stateIn(viewModelScope + Dispatchers.Default, SharingStarted.Lazily, false)
	val onPasswordSet = MutableEventFlow<Unit>()
	val onPasswordMismatch = MutableEventFlow<Unit>()
	val onClearText = MutableEventFlow<Unit>()

	val isBiometricEnabled
		get() = settings.isBiometricProtectionEnabled

	fun onNextClick(password: String) {
		if (firstPassword.value == null) {
			firstPassword.value = password
			onClearText.call(Unit)
		} else {
			if (firstPassword.value == password) {
				settings.appPassword = password.md5()
				settings.isAppPasswordNumeric = password.isNumeric()
				onPasswordSet.call(Unit)
			} else {
				onPasswordMismatch.call(Unit)
			}
		}
	}

	fun setBiometricEnabled(isEnabled: Boolean) {
		settings.isBiometricProtectionEnabled = isEnabled
	}
}
