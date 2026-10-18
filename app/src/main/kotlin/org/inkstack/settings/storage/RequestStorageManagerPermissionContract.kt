package org.inkstack.settings.storage

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.net.toUri

/** Opens the system "All files access" screen for this app. */
class RequestStorageManagerPermissionContract : ActivityResultContract<Unit, Boolean>() {

	override fun createIntent(context: Context, input: Unit): Intent {
		return Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
			addCategory(Intent.CATEGORY_DEFAULT)
			data = "package:${context.packageName}".toUri()
		}
	}

	override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
		return Environment.isExternalStorageManager()
	}

	override fun getSynchronousResult(context: Context, input: Unit): SynchronousResult<Boolean>? {
		return if (Environment.isExternalStorageManager()) {
			SynchronousResult(true)
		} else {
			null
		}
	}
}
