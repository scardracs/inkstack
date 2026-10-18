package org.inkstack.list.ui.compose

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import org.inkstack.R
import org.inkstack.core.model.isLocal
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.nav.router
import org.inkstack.core.util.ShareHelper
import org.inkstack.core.util.ext.findActivity
import org.inkstack.explore.ui.model.RecommendationsItem
import org.inkstack.list.ui.model.ListModel
import org.inkstack.list.ui.model.MangaListModel
import org.inkstack.parsers.model.Manga

/**
 * Multi-select for Compose manga grids, backed by AppCompat [ActionMode] so the existing
 * `mode_*` menus keep all quick actions (share, favourite, save, remove, …).
 */
@Stable
class MangaListSelectionState internal constructor(
	private val activity: AppCompatActivity?,
	private val contentProvider: () -> List<ListModel>,
	@MenuRes private val menuRes: Int,
	private val onCustomAction: (itemId: Int, ids: Set<Long>, manga: Set<Manga>, finish: () -> Unit) -> Boolean,
) {
	var selectedIds by mutableStateOf(emptySet<Long>())
		private set

	val isActive: Boolean
		get() = selectedIds.isNotEmpty()

	private var actionMode: ActionMode? = null

	fun selectedManga(): Set<Manga> = collectSelectedManga(contentProvider(), selectedIds)

	fun onMangaClick(id: Long, open: () -> Unit) {
		if (selectedIds.isEmpty()) {
			open()
		} else {
			toggle(id)
		}
	}

	fun onMangaLongClick(id: Long) {
		if (selectedIds.isEmpty()) {
			selectedIds = setOf(id)
			actionMode = activity?.startSupportActionMode(callback)
			if (actionMode == null) {
				selectedIds = emptySet()
			}
		} else {
			toggle(id)
		}
	}

	fun clear() {
		actionMode?.finish()
		selectedIds = emptySet()
		actionMode = null
	}

	private fun toggle(id: Long) {
		val next = if (id in selectedIds) selectedIds - id else selectedIds + id
		selectedIds = next
		if (next.isEmpty()) {
			actionMode?.finish()
			actionMode = null
		} else {
			actionMode?.invalidate()
		}
	}

	private fun selectAll() {
		selectedIds = contentProvider().mangaIds()
		actionMode?.invalidate()
	}

	private val callback = object : ActionMode.Callback {
		override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
			mode.menuInflater.inflate(menuRes, menu)
			return menu.hasVisibleItems()
		}

		override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
			mode.title = selectedIds.size.toString()
			val selected = selectedManga()
			val hasNoLocal = selected.isNotEmpty() && selected.none { it.isLocal }
			val hasSingleSource = selected.map { it.source }.distinct().size == 1
			val isSingle = selectedIds.size == 1
			menu.findItem(R.id.action_save)?.isVisible = hasNoLocal
			menu.findItem(R.id.action_replace_source)?.isVisible = hasNoLocal && hasSingleSource
			menu.findItem(R.id.action_edit_override)?.isVisible = isSingle
			return true
		}

		override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
			val ids = selectedIds
			val manga = selectedManga()
			val finish = { mode.finish() }
			if (item.itemId == R.id.action_select_all) {
				selectAll()
				return true
			}
			val context = activity ?: return false
			val router = (context as? FragmentActivity)?.router
			return handleCommonMangaQuickAction(
				itemId = item.itemId,
				context = context,
				router = router,
				manga = manga,
				finish = finish,
			) || onCustomAction(item.itemId, ids, manga, finish)
		}

		override fun onDestroyActionMode(mode: ActionMode) {
			selectedIds = emptySet()
			actionMode = null
		}
	}
}

@Composable
fun rememberMangaListSelection(
	content: List<ListModel>,
	@MenuRes menuRes: Int,
	onCustomAction: (itemId: Int, ids: Set<Long>, manga: Set<Manga>, finish: () -> Unit) -> Boolean =
		{ _, _, _, _ -> false },
): MangaListSelectionState {
	val context = LocalContext.current
	val activity = remember(context) { context.findActivity() as? AppCompatActivity }
	val contentState = rememberUpdatedState(content)
	val actionState = rememberUpdatedState(onCustomAction)

	val state = remember(activity, menuRes) {
		MangaListSelectionState(
			activity = activity,
			contentProvider = { contentState.value },
			menuRes = menuRes,
			onCustomAction = { itemId, ids, manga, finish ->
				actionState.value(itemId, ids, manga, finish)
			},
		)
	}

	DisposableEffect(state) {
		onDispose { state.clear() }
	}
	return state
}

fun handleCommonMangaQuickAction(
	itemId: Int,
	context: Context,
	router: AppRouter?,
	manga: Set<Manga>,
	finish: () -> Unit,
): Boolean {
	if (router == null || manga.isEmpty()) return false
	return when (itemId) {
		R.id.action_share -> {
			ShareHelper.shareMangaLinks(context, manga)
			finish()
			true
		}
		R.id.action_favourite -> {
			router.showFavoriteDialog(manga)
			finish()
			true
		}
		R.id.action_save -> {
			router.showDownloadDialog(manga, snackbarHost = null)
			finish()
			true
		}
		R.id.action_edit_override -> {
			router.openMangaOverrideConfig(manga.singleOrNull() ?: return false)
			finish()
			true
		}
		R.id.action_replace_source -> {
			router.openSourceReplacement(manga.toList())
			finish()
			true
		}
		else -> false
	}
}

private fun collectSelectedManga(content: List<ListModel>, ids: Set<Long>): Set<Manga> {
	if (ids.isEmpty()) return emptySet()
	val out = LinkedHashSet<Manga>(ids.size)
	for (item in content) {
		when (item) {
			is MangaListModel -> if (item.id in ids) out.add(item.manga)
			is RecommendationsItem -> item.manga.forEach { m ->
				if (m.id in ids) out.add(m.manga)
			}
		}
	}
	return out
}

private fun List<ListModel>.mangaIds(): Set<Long> = buildSet {
	for (item in this@mangaIds) {
		when (item) {
			is MangaListModel -> add(item.id)
			is RecommendationsItem -> item.manga.forEach { add(it.id) }
		}
	}
}
