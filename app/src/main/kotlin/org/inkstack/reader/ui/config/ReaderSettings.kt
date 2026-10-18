package org.inkstack.reader.ui.config

import android.graphics.Bitmap
import androidx.collection.scatterSetOf
import androidx.compose.runtime.Immutable
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.inkstack.core.model.ZoomMode
import org.inkstack.core.parser.MangaDataRepository
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.prefs.ReaderBackground
import org.inkstack.core.util.MediatorStateFlow
import org.inkstack.core.util.ext.processLifecycleScope
import org.inkstack.reader.domain.ReaderColorFilter

@Immutable
data class ReaderSettings(
	val zoomMode: ZoomMode,
	val background: ReaderBackground,
	val colorFilter: ReaderColorFilter?,
	val isReaderOptimizationEnabled: Boolean,
	val bitmapConfig: Bitmap.Config,
	val isPagesNumbersEnabled: Boolean,
	val isPagesCropEnabledWebtoon: Boolean,
) {

	private constructor(settings: AppSettings, colorFilterOverride: ReaderColorFilter?) : this(
		zoomMode = settings.zoomMode,
		background = settings.readerBackground,
		colorFilter = colorFilterOverride?.takeUnless { it.isEmpty } ?: settings.readerColorFilter,
		isReaderOptimizationEnabled = settings.isReaderOptimizationEnabled,
		bitmapConfig = if (settings.is32BitColorsEnabled) {
			Bitmap.Config.ARGB_8888
		} else {
			Bitmap.Config.RGB_565
		},
		isPagesNumbersEnabled = settings.isPagesNumbersEnabled,
		isPagesCropEnabledWebtoon = settings.isPagesCropEnabled(),
	)

	fun isPagesCropEnabled(isWebtoon: Boolean) = isPagesCropEnabledWebtoon

	fun composeColorFilter(): androidx.compose.ui.graphics.ColorFilter? {
		val cf = colorFilter?.takeUnless { it.isEmpty } ?: return null
		return androidx.compose.ui.graphics.ColorFilter.colorMatrix(
			androidx.compose.ui.graphics.ColorMatrix(cf.toColorMatrix().array.copyOf()),
		)
	}

	class Producer @AssistedInject constructor(
		@Assisted private val mangaId: Flow<Long>,
		private val settings: AppSettings,
		private val mangaDataRepository: MangaDataRepository,
	) : MediatorStateFlow<ReaderSettings>(ReaderSettings(settings, null)) {

		private val settingsKeys = scatterSetOf(
			AppSettings.KEY_ZOOM_MODE,
			AppSettings.KEY_PAGES_NUMBERS,
			AppSettings.KEY_READER_BACKGROUND,
			AppSettings.KEY_32BIT_COLOR,
			AppSettings.KEY_READER_OPTIMIZE,
			AppSettings.KEY_CF_CONTRAST,
			AppSettings.KEY_CF_BRIGHTNESS,
			AppSettings.KEY_CF_INVERTED,
			AppSettings.KEY_CF_GRAYSCALE,
			AppSettings.KEY_CF_BOOK,
			AppSettings.KEY_READER_CROP,
		)
		private var job: Job? = null

		override fun onActive() {
			assert(job?.isActive != true)
			job?.cancel()
			job = processLifecycleScope.launch(Dispatchers.Default) {
				observeImpl()
			}
		}

		override fun onInactive() {
			job?.cancel()
			job = null
		}

		private suspend fun observeImpl() {
			combine(
				mangaId.flatMapLatest { mangaDataRepository.observeColorFilter(it) },
				settings.observeChanges().filter { x -> x == null || x in settingsKeys }.onStart { emit(null) },
			) { mangaCf, _ ->
				ReaderSettings(settings, mangaCf)
			}.collect {
				publishValue(it)
			}
		}

		@AssistedFactory
		interface Factory {
			fun create(mangaId: Flow<Long>): Producer
		}
	}
}
