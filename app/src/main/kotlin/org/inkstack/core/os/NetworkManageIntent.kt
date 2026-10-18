package org.inkstack.core.os

import android.content.Intent
import android.provider.Settings

@Suppress("FunctionName")
fun NetworkManageIntent(): Intent {
	return Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
}
