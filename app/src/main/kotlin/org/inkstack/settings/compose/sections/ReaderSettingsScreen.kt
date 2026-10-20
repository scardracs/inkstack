package org.inkstack.settings.compose.sections

import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import org.inkstack.R
import org.inkstack.core.model.ZoomMode
import org.inkstack.core.nav.router
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.prefs.EInkFlashColor
import org.inkstack.core.prefs.ReaderAnimation
import org.inkstack.core.prefs.ReaderBackground
import org.inkstack.core.prefs.ReaderControl
import org.inkstack.core.util.ext.findActivity
import org.inkstack.core.util.ext.getQuantityStringSafe
import org.inkstack.parsers.util.mapToSet
import org.inkstack.parsers.util.names
import org.inkstack.settings.compose.prefs.ListPreferenceItem
import org.inkstack.settings.compose.prefs.MultiSelectPreferenceItem
import org.inkstack.settings.compose.prefs.NavigationPreferenceItem
import org.inkstack.settings.compose.prefs.PreferenceCategoryHeader
import org.inkstack.settings.compose.prefs.SliderPreferenceItem
import org.inkstack.settings.compose.prefs.SwitchPreferenceItem
import org.inkstack.settings.compose.prefs.rememberSharedPreferences

@Composable
fun ReaderSettingsScreen(
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val context = LocalContext.current
	val prefs = rememberSharedPreferences()
	val zoomModes = stringArrayResource(R.array.zoom_modes)
	val readerControls = stringArrayResource(R.array.reader_controls)
	val readerAnimation = stringArrayResource(R.array.reader_animation)
	val readerCrop = stringArrayResource(R.array.reader_crop)
	val readerCropValues = stringArrayResource(R.array.values_reader_crop)
	val orientations = stringArrayResource(R.array.screen_orientations)
	val backgrounds = stringArrayResource(R.array.reader_backgrounds)
	val networkPolicy = stringArrayResource(R.array.network_policy)
	val networkPolicyValues = stringArrayResource(R.array.values_network_policy)

	val controlValues = remember { ReaderControl.entries.names() }
	val defaultControls = remember { ReaderControl.DEFAULT.mapToSet { it.name } }
	val zoomValues = remember { ZoomMode.entries.names() }
	val animValues = remember { ReaderAnimation.entries.names() }
	val bgValues = remember { ReaderBackground.entries.names() }
	val flashColorValues = remember { EInkFlashColor.entries.names() }
	val orientationValues = remember {
		arrayOf(
			ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED.toString(),
			ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR.toString(),
			ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT.toString(),
			ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE.toString(),
		)
	}
	val flashColorEntries = arrayOf(
		stringResource(R.string.color_white),
		stringResource(R.string.color_black),
	)

	var zoomMode by remember {
		mutableStateOf(prefs.getString(AppSettings.KEY_ZOOM_MODE, ZoomMode.FIT_CENTER.name) ?: ZoomMode.FIT_CENTER.name)
	}
	var webtoonZoom by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_WEBTOON_ZOOM, true)) }
	var webtoonZoomOut by remember { mutableStateOf(prefs.getInt(AppSettings.KEY_WEBTOON_ZOOM_OUT, 0)) }
	var webtoonGaps by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_WEBTOON_GAPS, false)) }
	var controls by remember {
		mutableStateOf(prefs.getStringSet(AppSettings.KEY_READER_CONTROLS, defaultControls)?.toSet() ?: defaultControls)
	}
	var tapsLtr by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_READER_CONTROL_LTR, false)) }
	var volumeButtons by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_READER_VOLUME_BUTTONS, false)) }
	var navInverted by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_READER_NAVIGATION_INVERTED, false)) }
	var animation by remember {
		mutableStateOf(
			prefs.getString(AppSettings.KEY_READER_ANIMATION, ReaderAnimation.DEFAULT.name)
				?: ReaderAnimation.DEFAULT.name,
		)
	}
	var pullGesture by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_WEBTOON_PULL_GESTURE, false)) }
	var enhancedColors by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_32BIT_COLOR, false)) }
	var optimize by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_READER_OPTIMIZE, false)) }
	var crop by remember {
		mutableStateOf(prefs.getStringSet(AppSettings.KEY_READER_CROP, emptySet())?.toSet().orEmpty())
	}
	var einkFlash by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_EINK_FLASH, false)) }
	var flashDuration by remember { mutableStateOf(prefs.getInt(AppSettings.KEY_EINK_FLASH_DURATION, 300)) }
	var flashEvery by remember { mutableStateOf(prefs.getInt(AppSettings.KEY_EINK_FLASH_EVERY, 1)) }
	var flashColor by remember {
		mutableStateOf(
			prefs.getString(AppSettings.KEY_EINK_FLASH_COLOR, EInkFlashColor.WHITE.name)
				?: EInkFlashColor.WHITE.name,
		)
	}
	var fullscreen by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_READER_FULLSCREEN, true)) }
	var orientation by remember {
		mutableStateOf(
			prefs.getString(
				AppSettings.KEY_READER_ORIENTATION,
				ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED.toString(),
			) ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED.toString(),
		)
	}
	var screenOn by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_READER_SCREEN_ON, true)) }
	var multitask by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_READER_MULTITASK, false)) }
	var readerBar by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_READER_BAR, true)) }
	var barTransparent by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_READER_BAR_TRANSPARENT, true)) }
	var chapterToast by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_READER_CHAPTER_TOAST, true)) }
	var background by remember {
		mutableStateOf(
			prefs.getString(AppSettings.KEY_READER_BACKGROUND, ReaderBackground.DEFAULT.name)
				?: ReaderBackground.DEFAULT.name,
		)
	}
	var pagesNumbers by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_PAGES_NUMBERS, false)) }
	var pagesPreload by remember { mutableStateOf(prefs.getString(AppSettings.KEY_PAGES_PRELOAD, "2") ?: "2") }

	SettingsSectionScaffold(
		titleRes = R.string.reader_settings,
		onBack = onBack,
		modifier = modifier,
	) {
		item {
			ListPreferenceItem(
				titleRes = R.string.scale_mode,
				entries = zoomModes,
				entryValues = zoomValues,
				value = zoomMode,
				iconRes = R.drawable.ic_zoom_in,
				onValueChange = {
					zoomMode = it
					prefs.edit { putString(AppSettings.KEY_ZOOM_MODE, it) }
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.webtoon_zoom,
				summaryRes = R.string.webtoon_zoom_summary,
				iconRes = R.drawable.ic_reader_vertical,
				checked = webtoonZoom,
				onCheckedChange = {
					webtoonZoom = it
					prefs.edit { putBoolean(AppSettings.KEY_WEBTOON_ZOOM, it) }
				},
			)
		}
		if (webtoonZoom) {
			item {
				SliderPreferenceItem(
					titleRes = R.string.default_webtoon_zoom_out,
					value = webtoonZoomOut,
					valueFrom = 0,
					valueTo = 50,
					step = 10,
					iconRes = R.drawable.ic_size_large,
					summary = "$webtoonZoomOut%",
					onValueChange = {
						webtoonZoomOut = it
						prefs.edit { putInt(AppSettings.KEY_WEBTOON_ZOOM_OUT, it) }
					},
				)
			}
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.webtoon_gaps,
				summaryRes = R.string.webtoon_gaps_summary,
				iconRes = R.drawable.ic_split_horizontal,
				checked = webtoonGaps,
				onCheckedChange = {
					webtoonGaps = it
					prefs.edit { putBoolean(AppSettings.KEY_WEBTOON_GAPS, it) }
				},
			)
		}
		item {
			MultiSelectPreferenceItem(
				titleRes = R.string.reader_controls_in_bottom_bar,
				entries = readerControls,
				entryValues = controlValues,
				values = controls,
				iconRes = R.drawable.ic_tap,
				emptySummaryRes = R.string.none,
				onValuesChange = {
					controls = it
					prefs.edit { putStringSet(AppSettings.KEY_READER_CONTROLS, it) }
				},
			)
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.reader_actions,
				summaryRes = R.string.reader_actions_summary,
				iconRes = R.drawable.ic_tap_reorder,
				onClick = {
					(context.findActivity() as? FragmentActivity)?.router?.openReaderTapGridSettings()
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.reader_control_ltr,
				summaryRes = R.string.reader_control_ltr_summary,
				iconRes = R.drawable.ic_reader_rtl,
				checked = tapsLtr,
				onCheckedChange = {
					tapsLtr = it
					prefs.edit { putBoolean(AppSettings.KEY_READER_CONTROL_LTR, it) }
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.switch_pages_volume_buttons,
				summaryRes = R.string.switch_pages_volume_buttons_summary,
				iconRes = R.drawable.ic_gesture_vertical,
				checked = volumeButtons,
				onCheckedChange = {
					volumeButtons = it
					prefs.edit { putBoolean(AppSettings.KEY_READER_VOLUME_BUTTONS, it) }
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.reader_navigation_inverted,
				summaryRes = R.string.reader_navigation_inverted_summary,
				iconRes = R.drawable.ic_revert,
				checked = navInverted,
				onCheckedChange = {
					navInverted = it
					prefs.edit { putBoolean(AppSettings.KEY_READER_NAVIGATION_INVERTED, it) }
				},
			)
		}
		item {
			ListPreferenceItem(
				titleRes = R.string.pages_animation,
				entries = readerAnimation,
				entryValues = animValues,
				value = animation,
				iconRes = R.drawable.ic_auto_fix,
				onValueChange = {
					animation = it
					prefs.edit { putString(AppSettings.KEY_READER_ANIMATION, it) }
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.enable_pull_gesture_title,
				summaryRes = R.string.enable_pull_gesture_summary,
				iconRes = R.drawable.ic_gesture_vertical,
				checked = pullGesture,
				onCheckedChange = {
					pullGesture = it
					prefs.edit { putBoolean(AppSettings.KEY_WEBTOON_PULL_GESTURE, it) }
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.enhanced_colors,
				summaryRes = R.string.enhanced_colors_summary,
				iconRes = R.drawable.ic_appearance,
				checked = enhancedColors,
				onCheckedChange = {
					enhancedColors = it
					prefs.edit { putBoolean(AppSettings.KEY_32BIT_COLOR, it) }
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.reader_optimize,
				summaryRes = R.string.reader_optimize_summary,
				iconRes = R.drawable.ic_auto_fix,
				checked = optimize,
				onCheckedChange = {
					optimize = it
					prefs.edit { putBoolean(AppSettings.KEY_READER_OPTIMIZE, it) }
				},
			)
		}
		item {
			MultiSelectPreferenceItem(
				titleRes = R.string.crop_pages,
				entries = readerCrop,
				entryValues = readerCropValues,
				values = crop,
				iconRes = R.drawable.ic_crop,
				emptySummaryRes = R.string.disabled,
				onValuesChange = {
					crop = it
					prefs.edit { putStringSet(AppSettings.KEY_READER_CROP, it) }
				},
			)
		}
		item { PreferenceCategoryHeader(R.string.e_ink) }
		item {
			SwitchPreferenceItem(
				titleRes = R.string.flash_on_page_change,
				summaryRes = R.string.flash_on_page_change_summary,
				iconRes = R.drawable.ic_flash,
				checked = einkFlash,
				onCheckedChange = {
					einkFlash = it
					prefs.edit { putBoolean(AppSettings.KEY_EINK_FLASH, it) }
				},
			)
		}
		if (einkFlash) {
			item {
				SliderPreferenceItem(
					titleRes = R.string.flash_duration,
					value = flashDuration,
					valueFrom = 100,
					valueTo = 1000,
					step = 50,
					iconRes = R.drawable.ic_timer,
					summary = stringResource(R.string.milliseconds_pattern, flashDuration),
					onValueChange = {
						flashDuration = it
						prefs.edit { putInt(AppSettings.KEY_EINK_FLASH_DURATION, it) }
					},
				)
			}
			item {
				SliderPreferenceItem(
					titleRes = R.string.flash_every,
					value = flashEvery,
					valueFrom = 1,
					valueTo = 10,
					step = 1,
					iconRes = R.drawable.ic_timelapse,
					summary = context.resources.getQuantityStringSafe(R.plurals.pages, flashEvery, flashEvery),
					onValueChange = {
						flashEvery = it
						prefs.edit { putInt(AppSettings.KEY_EINK_FLASH_EVERY, it) }
					},
				)
			}
			item {
				ListPreferenceItem(
					titleRes = R.string.flash_with,
					entries = flashColorEntries,
					entryValues = flashColorValues,
					value = flashColor,
					iconRes = R.drawable.ic_appearance,
					onValueChange = {
						flashColor = it
						prefs.edit { putString(AppSettings.KEY_EINK_FLASH_COLOR, it) }
					},
				)
			}
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.fullscreen_mode,
				summaryRes = R.string.reader_fullscreen_summary,
				iconRes = R.drawable.ic_fullscreen,
				checked = fullscreen,
				onCheckedChange = {
					fullscreen = it
					prefs.edit { putBoolean(AppSettings.KEY_READER_FULLSCREEN, it) }
				},
			)
		}
		item {
			ListPreferenceItem(
				titleRes = R.string.screen_orientation,
				entries = orientations,
				entryValues = orientationValues,
				value = orientation,
				iconRes = R.drawable.ic_screen_rotation,
				onValueChange = {
					orientation = it
					prefs.edit { putString(AppSettings.KEY_READER_ORIENTATION, it) }
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.keep_screen_on,
				summaryRes = R.string.keep_screen_on_summary,
				iconRes = R.drawable.ic_eye,
				checked = screenOn,
				onCheckedChange = {
					screenOn = it
					prefs.edit { putBoolean(AppSettings.KEY_READER_SCREEN_ON, it) }
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.reader_multitask,
				summaryRes = R.string.reader_multitask_summary,
				iconRes = R.drawable.ic_split_horizontal,
				checked = multitask,
				onCheckedChange = {
					multitask = it
					prefs.edit { putBoolean(AppSettings.KEY_READER_MULTITASK, it) }
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.reader_info_bar,
				summaryRes = R.string.reader_info_bar_summary,
				iconRes = R.drawable.ic_info_outline,
				checked = readerBar,
				onCheckedChange = {
					readerBar = it
					prefs.edit { putBoolean(AppSettings.KEY_READER_BAR, it) }
				},
			)
		}
		if (readerBar) {
			item {
				SwitchPreferenceItem(
					titleRes = R.string.reader_info_bar_transparent,
					iconRes = R.drawable.ic_eye_off,
					checked = barTransparent,
					onCheckedChange = {
						barTransparent = it
						prefs.edit { putBoolean(AppSettings.KEY_READER_BAR_TRANSPARENT, it) }
					},
				)
			}
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.reader_chapter_toast,
				summaryRes = R.string.reader_chapter_toast_summary,
				iconRes = R.drawable.ic_notification,
				checked = chapterToast,
				onCheckedChange = {
					chapterToast = it
					prefs.edit { putBoolean(AppSettings.KEY_READER_CHAPTER_TOAST, it) }
				},
			)
		}
		item {
			ListPreferenceItem(
				titleRes = R.string.background,
				entries = backgrounds,
				entryValues = bgValues,
				value = background,
				iconRes = R.drawable.ic_appearance,
				onValueChange = {
					background = it
					prefs.edit { putString(AppSettings.KEY_READER_BACKGROUND, it) }
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.show_pages_numbers,
				summaryRes = R.string.show_pages_numbers_summary,
				iconRes = R.drawable.ic_book_page,
				checked = pagesNumbers,
				onCheckedChange = {
					pagesNumbers = it
					prefs.edit { putBoolean(AppSettings.KEY_PAGES_NUMBERS, it) }
				},
			)
		}
		item {
			ListPreferenceItem(
				titleRes = R.string.preload_pages,
				entries = networkPolicy,
				entryValues = networkPolicyValues,
				value = pagesPreload,
				iconRes = R.drawable.ic_download,
				onValueChange = {
					pagesPreload = it
					prefs.edit { putString(AppSettings.KEY_PAGES_PRELOAD, it) }
				},
			)
		}
	}
}
