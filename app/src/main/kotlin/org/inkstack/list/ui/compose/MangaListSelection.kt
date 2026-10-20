package org.inkstack.list.ui.compose

import android.content.Context
import android.view.Menu
import androidx.annotation.MenuRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.inkstack.R
import org.inkstack.core.model.isLocal
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.nav.router
import org.inkstack.core.ui.compose.SelectionChromeController
import org.inkstack.core.ui.compose.rememberSelectionChromeController
import org.inkstack.core.util.ShareHelper
import org.inkstack.core.util.ext.findActivity
import org.inkstack.explore.ui.model.RecommendationsItem
import org.inkstack.list.ui.model.ListModel
import org.inkstack.list.ui.model.MangaListModel
import org.inkstack.parsers.model.Manga

/**
 * Multi-select for Compose manga grids. Uses [SelectionChromeController] and reuses
 * existing `mode_*` menu XML for actions — no AppCompat ActionMode / View chrome.
 */
@Stable
class MangaListSelectionState internal constructor(
	private val controller: SelectionChromeController,
	private val contentProvider: () -> List<ListModel>,
) {
	val selectedIds: Set<Long>
		get() = controller.selectedIds

	val isActive: Boolean
		get() = controller.isActive

	fun selectedManga(): Set<Manga> = collectSelectedManga(contentProvider(), selectedIds)

	fun onMangaClick(id: Long, open: () -> Unit) = controller.onItemClick(id, open)

	fun onMangaLongClick(id: Long) = controller.onItemLongClick(id)

	fun clear() = controller.clear()
}

@Composable
fun rememberMangaListSelection(
	content: List<ListModel>,
	@MenuRes menuRes: Int,
	onCustomAction: (itemId: Int, ids: Set<Long>, manga: Set<Manga>, finish: () -> Unit) -> Boolean =
		{ _, _, _, _ -> false },
): MangaListSelectionState {
	val context = LocalContext.current
	val activity = remember(context) { context.findActivity() as? FragmentActivity }
	val contentState = rememberUpdatedState(content)
	val actionState = rememberUpdatedState(onCustomAction)

	val controller = rememberSelectionChromeController(
		menuRes = menuRes,
		collectAllIds = { contentState.value.mangaIds() },
		prepareMenu = { menu, ids ->
			prepareMangaSelectionMenu(menu, collectSelectedManga(contentState.value, ids), ids)
		},
		handleAction = { itemId, ids, finish ->
			val manga = collectSelectedManga(contentState.value, ids)
			handleCommonMangaQuickAction(
				itemId = itemId,
				context = context,
				router = activity?.router,
				manga = manga,
				finish = finish,
			) || actionState.value(itemId, ids, manga, finish)
		},
	)

	return remember(controller) {
		MangaListSelectionState(
			controller = controller,
			contentProvider = { contentState.value },
		)
	}
}

fun confirmMarkAsCompleted(
	context: Context,
	manga: Set<Manga>,
	finish: () -> Unit,
	onConfirm: (Set<Manga>) -> Unit,
): Boolean {
	if (manga.isEmpty()) return false
	MaterialAlertDialogBuilder(context)
		.setTitle(R.string.mark_as_completed)
		.setMessage(R.string.mark_as_completed_prompt)
		.setNegativeButton(android.R.string.cancel, null)
		.setPositiveButton(android.R.string.ok) { _, _ ->
			onConfirm(manga)
			finish()
		}
		.show()
	return true
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

private fun prepareMangaSelectionMenu(menu: Menu, selected: Set<Manga>, ids: Set<Long>) {
	val hasNoLocal = selected.isNotEmpty() && selected.none { it.isLocal }
	val hasSingleSource = selected.map { it.source }.distinct().size == 1
	val isSingle = ids.size == 1
	menu.findItem(R.id.action_save)?.isVisible = hasNoLocal
	menu.findItem(R.id.action_replace_source)?.isVisible = hasNoLocal && hasSingleSource
	menu.findItem(R.id.action_edit_override)?.isVisible = isSingle
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
