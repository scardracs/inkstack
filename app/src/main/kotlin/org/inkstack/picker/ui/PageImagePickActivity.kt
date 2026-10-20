package org.inkstack.picker.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import coil3.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.BuildConfig
import org.inkstack.core.model.parcelable.ParcelableManga
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.core.util.ext.getParcelableExtraCompat
import org.inkstack.picker.ui.compose.PageImagePickScreen
import org.inkstack.reader.ui.PageSaveHelper
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class PageImagePickActivity : BaseComposeActivity() {

	@Inject
	lateinit var pageSaveHelperFactory: PageSaveHelper.Factory

	@Inject
	lateinit var imageLoader: ImageLoader

	private val viewModel by viewModels<PageImagePickViewModel>()
	private lateinit var pageSaveHelper: PageSaveHelper
	private val launchedWithManga = lazy {
		intent?.hasExtra(AppRouter.KEY_MANGA) == true || intent?.hasExtra(AppRouter.KEY_ID) == true
	}
	private var selectedMangaId by mutableStateOf<Long?>(null)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		pageSaveHelper = pageSaveHelperFactory.create(this)
		selectedMangaId = intent?.getParcelableExtraCompat<ParcelableManga>(AppRouter.KEY_MANGA)?.manga?.id
			?: intent?.getLongExtra(AppRouter.KEY_ID, -1L)?.takeIf { it > 0 }
		setComposeContent {
			PageImagePickScreen(
				mangaId = selectedMangaId,
				onBack = {
					if (selectedMangaId != null && !launchedWithManga.value) {
						selectedMangaId = null
					} else {
						dispatchNavigateUp()
					}
				},
				onMangaPicked = { manga ->
					intent.putExtra(AppRouter.KEY_ID, manga.id)
					selectedMangaId = manga.id
				},
				onFileReady = ::finishWithResult,
				pageSaveHelper = pageSaveHelper,
				imageLoader = imageLoader,
				pickViewModel = viewModel,
			)
		}
	}

	private fun finishWithResult(file: File) {
		val uri = FileProvider.getUriForFile(applicationContext, "${BuildConfig.APPLICATION_ID}.files", file)
		val result = Intent()
		result.setData(uri)
		result.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
		setResult(RESULT_OK, result)
		finish()
	}
}
