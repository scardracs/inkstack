package org.inkstack.reader.ui.config

import android.os.Bundle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.inkstack.R
import org.inkstack.core.nav.router
import org.inkstack.core.parser.MangaRepository
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.ui.compose.sheet.ComposeAdaptiveSheet
import org.inkstack.core.util.ext.findParentCallback
import org.inkstack.reader.domain.PageLoader
import org.inkstack.reader.ui.ReaderViewModel
import org.inkstack.reader.ui.ScreenOrientationHelper
import javax.inject.Inject

@AndroidEntryPoint
class ReaderConfigSheet : ComposeAdaptiveSheet() {

	private val viewModel by activityViewModels<ReaderViewModel>()

	@Inject
	lateinit var orientationHelper: ScreenOrientationHelper

	@Inject
	lateinit var mangaRepositoryFactory: MangaRepository.Factory

	@Inject
	lateinit var pageLoader: PageLoader

	@Inject
	lateinit var settings: AppSettings

	private lateinit var imageServerDelegate: ImageServerDelegate

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		imageServerDelegate = ImageServerDelegate(
			mangaRepositoryFactory = mangaRepositoryFactory,
			mangaSource = viewModel.getMangaOrNull()?.source,
		)
	}

	@Composable
	override fun sheetTitle(): String = stringResource(R.string.options)

	@Composable
	override fun SheetContent(onDismissRequest: () -> Unit) {
		val scope = rememberCoroutineScope()
		val context = LocalContext.current
		val isBookmarkAdded by viewModel.isBookmarkAdded.collectAsState()
		val autoOrientation by orientationHelper.observeAutoOrientation()
			.collectAsState(initial = false)
		var imageServerAvailable by remember { mutableStateOf(false) }
		var imageServerLabel by remember { mutableStateOf<String?>(null) }
		var rotationLocked by remember { mutableStateOf(orientationHelper.isLocked) }

		LaunchedEffect(Unit) {
			imageServerAvailable = imageServerDelegate.isAvailable()
			if (imageServerAvailable) {
				imageServerLabel = imageServerDelegate.getValue()
			}
		}

		Column(
			modifier = Modifier
				.fillMaxWidth()
				.verticalScroll(rememberScrollState())
				.padding(vertical = 4.dp),
		) {
			SheetActionRow(
				icon = R.drawable.ic_save,
				label = stringResource(R.string.save_page),
				onClick = {
					findParentCallback(Callback::class.java)?.onSavePageClick() ?: return@SheetActionRow
					onDismissRequest()
				},
			)
			SheetActionRow(
				icon = if (isBookmarkAdded) R.drawable.ic_bookmark_checked else R.drawable.ic_bookmark,
				label = stringResource(
					if (isBookmarkAdded) R.string.bookmark_remove else R.string.bookmark_add,
				),
				onClick = { viewModel.toggleBookmark() },
			)
			if (imageServerAvailable) {
				val serverValue = imageServerLabel ?: stringResource(R.string.automatic)
				SheetActionRow(
					icon = R.drawable.ic_images,
					label = stringResource(
						R.string.inline_preference_pattern,
						stringResource(R.string.image_server),
						serverValue,
					),
					onClick = {
						scope.launch {
							if (imageServerDelegate.showDialog(context)) {
								imageServerLabel = imageServerDelegate.getValue()
								pageLoader.invalidate(clearCache = true)
								viewModel.switchChapterBy(0)
							}
						}
					},
				)
			}
			if (!autoOrientation) {
				SheetActionRow(
					icon = R.drawable.ic_screen_rotation,
					label = stringResource(R.string.rotate_screen),
					onClick = {
						orientationHelper.isLandscape = !orientationHelper.isLandscape
					},
				)
			} else {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.clickable {
							rotationLocked = !rotationLocked
							orientationHelper.isLocked = rotationLocked
						}
						.padding(horizontal = 16.dp, vertical = 12.dp),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(16.dp),
				) {
					Icon(
						painter = painterResource(R.drawable.ic_screen_rotation_lock),
						contentDescription = null,
						modifier = Modifier.size(24.dp),
						tint = MaterialTheme.colorScheme.onSurfaceVariant,
					)
					Text(
						text = stringResource(R.string.lock_screen_rotation),
						style = MaterialTheme.typography.bodyLarge,
						modifier = Modifier.weight(1f),
					)
					Switch(
						checked = rotationLocked,
						onCheckedChange = {
							rotationLocked = it
							orientationHelper.isLocked = it
						},
					)
				}
			}
			SheetActionRow(
				icon = R.drawable.ic_timer,
				label = stringResource(R.string.automatic_scroll),
				onClick = {
					findParentCallback(Callback::class.java)?.onScrollTimerClick(false)
						?: return@SheetActionRow
					onDismissRequest()
				},
			)
			SheetActionRow(
				icon = R.drawable.ic_appearance,
				label = stringResource(R.string.color_correction),
				onClick = {
					val page = viewModel.getCurrentPage() ?: return@SheetActionRow
					val manga = viewModel.getMangaOrNull() ?: return@SheetActionRow
					router.openColorFilterConfig(manga, page)
				},
			)
			SheetActionRow(
				icon = R.drawable.ic_settings,
				label = stringResource(R.string.settings),
				onClick = {
					router.openReaderSettings()
					onDismissRequest()
				},
			)
		}
	}

	interface Callback {
		fun onSavePageClick()
		fun onScrollTimerClick(isLongClick: Boolean)
		fun onBookmarkClick()
	}
}

@Composable
private fun SheetActionRow(
	icon: Int,
	label: String,
	onClick: () -> Unit,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(horizontal = 16.dp, vertical = 14.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(16.dp),
	) {
		Icon(
			painter = painterResource(icon),
			contentDescription = null,
			modifier = Modifier.size(24.dp),
			tint = MaterialTheme.colorScheme.onSurfaceVariant,
		)
		Text(
			text = label,
			style = MaterialTheme.typography.bodyLarge,
			modifier = Modifier.weight(1f),
		)
	}
}
