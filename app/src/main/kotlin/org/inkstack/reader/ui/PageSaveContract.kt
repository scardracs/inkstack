package org.inkstack.reader.ui

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.preference.PreferenceManager
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.util.MimeTypes
import org.inkstack.core.util.ext.toUriOrNull
import java.io.File

class PageSaveContract : ActivityResultContracts.CreateDocument("image/*") {

	override fun createIntent(context: Context, input: String): Intent {
		val intent = super.createIntent(context, input.substringAfterLast(File.separatorChar))
		intent.type = MimeTypes.getMimeTypeFromExtension(input)?.toString() ?: "image/*"
		val defaultUri = input.toUriOrNull()?.run {
			path?.let { p ->
				buildUpon().path(p.substringBeforeLast('/')).build()
			}
		}
		intent.putExtra(
			DocumentsContract.EXTRA_INITIAL_URI,
			defaultUri ?: resolveFallbackInitialUri(context),
		)
		return intent
	}

	private fun resolveFallbackInitialUri(context: Context): android.net.Uri {
		val pagesSaveUri = PreferenceManager.getDefaultSharedPreferences(context)
			.getString(AppSettings.KEY_PAGES_SAVE_DIR, null)
			?.toUriOrNull()
			?.let { DocumentFile.fromTreeUri(context, it)?.uri }
		if (pagesSaveUri != null) {
			return pagesSaveUri
		}
		return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.toUri()
			?: File(context.filesDir, Environment.DIRECTORY_PICTURES).apply { mkdirs() }.toUri()
	}
}
