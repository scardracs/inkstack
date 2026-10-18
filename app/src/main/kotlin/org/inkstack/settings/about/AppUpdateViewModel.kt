package org.inkstack.settings.about

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import org.inkstack.BuildConfig
import org.inkstack.R
import org.inkstack.core.github.AppUpdateRepository
import org.inkstack.core.ui.BaseViewModel
import org.inkstack.core.util.ext.MutableEventFlow
import org.inkstack.core.util.ext.call
import org.inkstack.core.util.ext.requireValue
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AppUpdateViewModel @Inject constructor(
	private val repository: AppUpdateRepository,
	@ApplicationContext private val context: Context,
) : BaseViewModel() {

	val nextVersion = repository.observeAvailableUpdate()
	val downloadProgress = MutableStateFlow(-1f)
	val downloadState = MutableStateFlow(DownloadManager.STATUS_PENDING)
	val installIntent = MutableStateFlow<Intent?>(null)
	val onDownloadDone = MutableEventFlow<Intent>()

	private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
	private val appName = context.getString(R.string.app_name)
	private var downloadDestination: File? = null

	init {
		if (nextVersion.value == null) {
			launchLoadingJob(Dispatchers.Default) {
				repository.fetchUpdate()
			}
		}
	}

	fun startDownload() {
		launchLoadingJob(Dispatchers.Default) {
			val version = nextVersion.requireValue()
			val url = version.apkUrl.toUri()
			val fileName = url.lastPathSegment ?: "inkstack-update.apk"
			val destinationDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
				?: context.cacheDir
			val destinationFile = File(destinationDir, fileName)
			downloadDestination = destinationFile
			val request = DownloadManager.Request(url)
				.setTitle("$appName v${version.name}")
				.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
				.setMimeType("application/vnd.android.package-archive")
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				request.setDestinationInExternalFilesDir(
					context,
					Environment.DIRECTORY_DOWNLOADS,
					fileName,
				)
			} else {
				@Suppress("DEPRECATION")
				request.setDestinationUri(destinationFile.toUri())
			}
			val downloadId = downloadManager.enqueue(request)
			observeDownload(downloadId)
		}
	}

	fun onDownloadComplete(intent: Intent) {
		launchLoadingJob(Dispatchers.Default) {
			val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L)
			if (downloadId == 0L) {
				return@launchLoadingJob
			}
			val destination = downloadDestination
			val contentUri = if (destination != null && destination.isFile) {
				FileProvider.getUriForFile(
					context,
					"${BuildConfig.APPLICATION_ID}.files",
					destination,
				)
			} else {
				downloadManager.getUriForDownloadedFile(downloadId)
			} ?: return@launchLoadingJob
			@Suppress("DEPRECATION")
			val installerIntent = Intent(Intent.ACTION_INSTALL_PACKAGE)
			installerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
			installerIntent.setDataAndType(
				contentUri,
				downloadManager.getMimeTypeForDownloadedFile(downloadId)
					?: "application/vnd.android.package-archive",
			)
			installerIntent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
			installIntent.value = installerIntent
			onDownloadDone.call(installerIntent)
		}
	}

	private suspend fun observeDownload(id: Long) {
		val query = DownloadManager.Query()
		query.setFilterById(id)
		while (currentCoroutineContext().isActive) {
			downloadManager.query(query).use { cursor ->
				if (cursor.moveToFirst()) {
					val bytesDownloaded = cursor.getLong(
						cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR),
					)
					val bytesTotal = cursor.getLong(
						cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES),
					)
					downloadProgress.value = bytesDownloaded.toFloat() / bytesTotal
					val state = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
					downloadState.value = state
					if (state == DownloadManager.STATUS_SUCCESSFUL || state == DownloadManager.STATUS_FAILED) {
						return
					}
				}
			}
			delay(100)
		}
	}
}
