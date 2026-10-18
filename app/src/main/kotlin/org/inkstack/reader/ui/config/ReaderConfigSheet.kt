package org.inkstack.reader.ui.config

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.inkstack.R
import org.inkstack.core.nav.router
import org.inkstack.core.parser.MangaRepository
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.ui.sheet.BaseAdaptiveSheet
import org.inkstack.core.util.ext.consume
import org.inkstack.core.util.ext.findParentCallback
import org.inkstack.core.util.ext.observe
import org.inkstack.core.util.ext.viewLifecycleScope
import org.inkstack.databinding.SheetReaderConfigBinding
import org.inkstack.reader.domain.PageLoader
import org.inkstack.reader.ui.ReaderViewModel
import org.inkstack.reader.ui.ScreenOrientationHelper
import javax.inject.Inject

@AndroidEntryPoint
class ReaderConfigSheet :
	BaseAdaptiveSheet<SheetReaderConfigBinding>(),
	View.OnClickListener,
	CompoundButton.OnCheckedChangeListener {

	private val viewModel by activityViewModels<ReaderViewModel>()

	@Inject
	lateinit var orientationHelper: ScreenOrientationHelper

	@Inject
	lateinit var mangaRepositoryFactory: MangaRepository.Factory

	@Inject
	lateinit var pageLoader: PageLoader

	private lateinit var imageServerDelegate: ImageServerDelegate

	@Inject
	lateinit var settings: AppSettings

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		imageServerDelegate = ImageServerDelegate(
			mangaRepositoryFactory = mangaRepositoryFactory,
			mangaSource = viewModel.getMangaOrNull()?.source,
		)
	}

	override fun onCreateViewBinding(
		inflater: LayoutInflater,
		container: ViewGroup?,
	): SheetReaderConfigBinding {
		return SheetReaderConfigBinding.inflate(inflater, container, false)
	}

	override fun onViewBindingCreated(
		binding: SheetReaderConfigBinding,
		savedInstanceState: Bundle?,
	) {
		super.onViewBindingCreated(binding, savedInstanceState)
		observeScreenOrientation()
		binding.buttonSavePage.setOnClickListener(this)
		binding.buttonScreenRotate.setOnClickListener(this)
		binding.buttonSettings.setOnClickListener(this)
		binding.buttonImageServer.setOnClickListener(this)
		binding.buttonColorFilter.setOnClickListener(this)
		binding.buttonScrollTimer.setOnClickListener(this)
		binding.buttonBookmark.setOnClickListener(this)
		binding.switchScreenLockRotation.setOnCheckedChangeListener(this)

		viewModel.isBookmarkAdded.observe(viewLifecycleOwner) {
			binding.buttonBookmark.setText(if (it) R.string.bookmark_remove else R.string.bookmark_add)
			binding.buttonBookmark.setCompoundDrawablesRelativeWithIntrinsicBounds(
				if (it) R.drawable.ic_bookmark_checked else R.drawable.ic_bookmark, 0, 0, 0,
			)
		}

		viewLifecycleScope.launch {
			val isAvailable = imageServerDelegate.isAvailable()
			if (isAvailable) {
				bindImageServerTitle()
			}
			binding.buttonImageServer.isVisible = isAvailable
		}
	}

	override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
		val typeMask = WindowInsetsCompat.Type.systemBars()
		viewBinding?.scrollView?.updatePadding(
			bottom = insets.getInsets(typeMask).bottom,
		)
		return insets.consume(v, typeMask, bottom = true)
	}

	override fun onClick(v: View) {
		when (v.id) {
			R.id.button_settings -> {
				router.openReaderSettings()
				dismissAllowingStateLoss()
			}

			R.id.button_scroll_timer -> {
				findParentCallback(Callback::class.java)?.onScrollTimerClick(false) ?: return
				dismissAllowingStateLoss()
			}

			R.id.button_save_page -> {
				findParentCallback(Callback::class.java)?.onSavePageClick() ?: return
				dismissAllowingStateLoss()
			}

			R.id.button_screen_rotate -> {
				orientationHelper.isLandscape = !orientationHelper.isLandscape
			}

			R.id.button_bookmark -> {
				viewModel.toggleBookmark()
			}

			R.id.button_color_filter -> {
				val page = viewModel.getCurrentPage() ?: return
				val manga = viewModel.getMangaOrNull() ?: return
				router.openColorFilterConfig(manga, page)
			}

			R.id.button_image_server -> viewLifecycleScope.launch {
				if (imageServerDelegate.showDialog(v.context)) {
					bindImageServerTitle()
					pageLoader.invalidate(clearCache = true)
					viewModel.switchChapterBy(0)
				}
			}
		}
	}

	override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
		when (buttonView.id) {
			R.id.switch_screen_lock_rotation -> {
				orientationHelper.isLocked = isChecked
			}
		}
	}

	private fun observeScreenOrientation() {
		orientationHelper.observeAutoOrientation()
			.onEach {
				with(requireViewBinding()) {
					buttonScreenRotate.isGone = it
					switchScreenLockRotation.isVisible = it
					updateOrientationLockSwitch()
				}
			}.launchIn(viewLifecycleScope)
	}

	private fun updateOrientationLockSwitch() {
		val switch = viewBinding?.switchScreenLockRotation ?: return
		switch.setOnCheckedChangeListener(null)
		switch.isChecked = orientationHelper.isLocked
		switch.setOnCheckedChangeListener(this)
	}

	private suspend fun bindImageServerTitle() {
		viewBinding?.buttonImageServer?.text = getString(
			R.string.inline_preference_pattern,
			getString(R.string.image_server),
			imageServerDelegate.getValue() ?: getString(R.string.automatic),
		)
	}

	interface Callback {
		fun onSavePageClick()
		fun onScrollTimerClick(isLongClick: Boolean)
		fun onBookmarkClick()
	}
}
