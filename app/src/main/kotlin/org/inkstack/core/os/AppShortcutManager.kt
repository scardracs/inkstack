package org.inkstack.core.os

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ShortcutManager
import androidx.annotation.VisibleForTesting
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.room.InvalidationTracker
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.transformations
import coil3.size.Scale
import coil3.size.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.inkstack.R
import org.inkstack.core.LocalizedAppContext
import org.inkstack.core.db.TABLE_HISTORY
import org.inkstack.core.model.getTitle
import org.inkstack.core.model.isNsfw
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.nav.ReaderIntent
import org.inkstack.core.parser.MangaDataRepository
import org.inkstack.core.parser.favicon.faviconUri
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.ui.image.ThumbnailTransformation
import org.inkstack.core.util.ext.getDrawableOrThrow
import org.inkstack.core.util.ext.mangaSourceExtra
import org.inkstack.core.util.ext.printStackTraceDebug
import org.inkstack.core.util.ext.processLifecycleScope
import org.inkstack.history.data.HistoryRepository
import org.inkstack.parsers.model.Manga
import org.inkstack.parsers.model.MangaSource
import org.inkstack.parsers.util.ifNullOrEmpty
import org.inkstack.parsers.util.mapNotNullToSet
import org.inkstack.parsers.util.runCatchingCancellable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppShortcutManager @Inject constructor(
	@LocalizedAppContext private val context: Context,
	private val coil: ImageLoader,
	private val historyRepository: HistoryRepository,
	private val mangaRepository: MangaDataRepository,
	private val settings: AppSettings,
) : InvalidationTracker.Observer(TABLE_HISTORY), SharedPreferences.OnSharedPreferenceChangeListener {

	private companion object {

		/**
		 * How much extra history to read when adult titles are being filtered out.
		 */
		const val HISTORY_OVERFETCH = 4
	}

	private val iconSize by lazy {
		Size(ShortcutManagerCompat.getIconMaxWidth(context), ShortcutManagerCompat.getIconMaxHeight(context))
	}
	private var shortcutsUpdateJob: Job? = null

	init {
		settings.subscribe(this)
	}

	override fun onInvalidated(tables: Set<String>) {
		if (!settings.isDynamicShortcutsEnabled) {
			return
		}
		val prevJob = shortcutsUpdateJob
		shortcutsUpdateJob = processLifecycleScope.launch(Dispatchers.Default) {
			prevJob?.join()
			updateShortcutsImpl()
		}
	}

	override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
		when (key) {
			AppSettings.KEY_SHORTCUTS -> if (settings.isDynamicShortcutsEnabled) {
				onInvalidated(emptySet())
			} else {
				clearShortcuts()
			}

			// Rebuild straight away, so turning the filter on removes what is already on the home screen
			// instead of waiting for the next history change.
			AppSettings.KEY_SHORTCUTS_NO_NSFW,
			AppSettings.KEY_DISABLE_NSFW -> if (settings.isDynamicShortcutsEnabled) {
				onInvalidated(emptySet())
			}
		}
	}

	suspend fun requestPinShortcut(manga: Manga): Boolean = try {
		ShortcutManagerCompat.requestPinShortcut(context, buildShortcutInfo(manga), null)
	} catch (e: IllegalStateException) {
		e.printStackTraceDebug()
		false
	}

	suspend fun requestPinShortcut(source: MangaSource): Boolean = try {
		ShortcutManagerCompat.requestPinShortcut(context, buildShortcutInfo(source), null)
	} catch (e: IllegalStateException) {
		e.printStackTraceDebug()
		false
	}

	fun getMangaShortcuts(): Set<Long> {
		val shortcuts = ShortcutManagerCompat.getShortcuts(
			context,
			ShortcutManagerCompat.FLAG_MATCH_CACHED or ShortcutManagerCompat.FLAG_MATCH_PINNED or ShortcutManagerCompat.FLAG_MATCH_DYNAMIC,
		)
		return shortcuts.mapNotNullToSet { it.id.toLongOrNull() }
	}

	@VisibleForTesting
	suspend fun await(): Boolean {
		return shortcutsUpdateJob?.join() != null
	}

	fun notifyMangaOpened(mangaId: Long) {
		ShortcutManagerCompat.reportShortcutUsed(context, mangaId.toString())
	}

	fun isDynamicShortcutsAvailable(): Boolean {
		return context.getSystemService(ShortcutManager::class.java).maxShortcutCountPerActivity > 0
	}

	private suspend fun updateShortcutsImpl() = runCatchingCancellable {
		val maxShortcuts = ShortcutManagerCompat.getMaxShortcutCountPerActivity(context).coerceAtLeast(5)
		val skipNsfw = settings.isShortcutsNsfwDisabled || settings.isNsfwContentDisabled
		// Over-fetch when filtering, so a run of adult titles at the top of the history does not leave
		// the launcher with only one or two shortcuts.
		val limit = if (skipNsfw) maxShortcuts * HISTORY_OVERFETCH else maxShortcuts
		val shortcuts = historyRepository.getList(0, limit)
			.filter { x -> x.title.isNotEmpty() && (!skipNsfw || !x.isNsfw()) }
			.take(maxShortcuts)
			.map { buildShortcutInfo(it) }
		ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
	}.onFailure {
		it.printStackTraceDebug()
	}

	private fun clearShortcuts() {
		try {
			ShortcutManagerCompat.removeAllDynamicShortcuts(context)
		} catch (_: IllegalStateException) {
		}
	}

	private suspend fun buildShortcutInfo(manga: Manga): ShortcutInfoCompat = withContext(Dispatchers.Default) {
		val icon = runCatchingCancellable {
			coil.execute(
				ImageRequest.Builder(context)
					.data(manga.coverUrl)
					.size(iconSize)
					.mangaSourceExtra(manga.source)
					.scale(Scale.FILL)
					.transformations(ThumbnailTransformation())
					.build(),
			).getDrawableOrThrow().toBitmap()
		}.fold(
			onSuccess = { IconCompat.createWithAdaptiveBitmap(it) },
			onFailure = { IconCompat.createWithResource(context, R.drawable.ic_shortcut_default) },
		)
		mangaRepository.storeManga(manga, replaceExisting = true)
		val title = manga.title.ifEmpty {
			manga.altTitles.firstOrNull()
		}.ifNullOrEmpty {
			context.getString(R.string.unknown)
		}
		ShortcutInfoCompat.Builder(context, manga.id.toString())
			.setShortLabel(title)
			.setLongLabel(title)
			.setIcon(icon)
			.setLongLived(true)
			.setIntent(
				ReaderIntent.Builder(context)
					.mangaId(manga.id)
					.build()
					.intent,
			).build()
	}

	private suspend fun buildShortcutInfo(source: MangaSource): ShortcutInfoCompat = withContext(Dispatchers.Default) {
		val icon = runCatchingCancellable {
			coil.execute(
				ImageRequest.Builder(context)
					.data(source.faviconUri())
					.mangaSourceExtra(source)
					.size(iconSize)
					.scale(Scale.FIT)
					.build(),
			).getDrawableOrThrow().toBitmap()
		}.fold(
			onSuccess = { IconCompat.createWithAdaptiveBitmap(it) },
			onFailure = { IconCompat.createWithResource(context, R.drawable.ic_shortcut_default) },
		)
		val title = source.getTitle(context)
		ShortcutInfoCompat.Builder(context, source.name)
			.setShortLabel(title)
			.setLongLabel(title)
			.setIcon(icon)
			.setLongLived(true)
			.setIntent(AppRouter.listIntent(context, source, null, null))
			.build()
	}
}
