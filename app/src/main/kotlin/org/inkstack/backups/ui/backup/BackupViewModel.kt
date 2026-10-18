package org.inkstack.backups.ui.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import org.inkstack.backups.data.BackupRepository
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.ui.BaseViewModel
import org.inkstack.core.util.ext.MutableEventFlow
import org.inkstack.core.util.ext.call
import org.inkstack.core.util.ext.require
import org.inkstack.core.util.progress.Progress
import java.util.zip.Deflater
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class BackupViewModel @Inject constructor(
	savedStateHandle: SavedStateHandle,
	private val repository: BackupRepository,
	@ApplicationContext context: Context,
) : BaseViewModel() {

	val progress = MutableStateFlow(Progress.INDETERMINATE)
	val onBackupDone = MutableEventFlow<Uri>()

	private val destination = savedStateHandle.require<Uri>(AppRouter.KEY_DATA)
	private val contentResolver: ContentResolver = context.contentResolver

	init {
		launchLoadingJob(Dispatchers.Default) {
			try {
				ZipOutputStream(checkNotNull(contentResolver.openOutputStream(destination))).use {
					it.setLevel(Deflater.BEST_COMPRESSION)
					repository.createBackup(it, progress)
				}
				onBackupDone.call(destination)
			} catch (e: CancellationException) {
				throw e
			} catch (e: Exception) {
				runCatching { contentResolver.delete(destination, null, null) }
				throw e
			}
		}
	}
}
