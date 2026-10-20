package org.inkstack.list.ui.compose

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
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
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.core.ui.compose.SelectionChromeAction
import org.inkstack.core.ui.compose.SelectionChromeHost
import org.inkstack.core.ui.compose.SelectionChromeSession
import org.inkstack.core.util.ShareHelper
import org.inkstack.core.util.ext.findActivity
import org.inkstack.explore.ui.model.RecommendationsItem
import org.inkstack.list.ui.model.ListModel
import org.inkstack.list.ui.model.MangaListModel
import org.inkstack.parsers.model.Manga

/**
 * Multi-select for Compose manga grids. Uses [SelectionChromeHost] (Compose glass bar) and reuses
 * existing `mode_*` menu XML for actions — no AppCompat ActionMode / View chrome.
 */
@Stable
class MangaListSelectionState internal constructor(
	private val activity: FragmentActivity?,
	private val chrome: SelectionChromeHost?,
	private val contentProvider: () -> List<ListModel>,
	@MenuRes private val menuRes: Int,
	private val onCustomAction: (itemId: Int, ids: Set<Long>, manga: Set<Manga>, finish: () -> Unit) -> Boolean,
) {
	var selectedIds by mutableStateOf(emptySet<Long>())
		private set

	val isActive: Boolean
		get() = selectedIds.isNotEmpty()

	private var session: MangaSelectionSession? = null

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
			startSession()
		} else {
			toggle(id)
		}
	}

	fun clear() {
		val active = session
		session = null
		selectedIds = emptySet()
		if (active != null) {
			chrome?.hide(active)
		}
	}

	private fun toggle(id: Long) {
		val next = if (id in selectedIds) selectedIds - id else selectedIds + id
		selectedIds = next
		if (next.isEmpty()) {
			clear()
		} else {
			session?.publish()
		}
	}

	private fun selectAll() {
		selectedIds = contentProvider().mangaIds()
		session?.publish()
	}

	private fun startSession() {
		val host = chrome ?: run {
			selectedIds = emptySet()
			return
		}
		val ctx = activity ?: run {
			selectedIds = emptySet()
			return
		}
		val newSession = MangaSelectionSession(ctx)
		session = newSession
		newSession.publish()
		host.show(newSession)
	}

	private inner class MangaSelectionSession(
		private val context: Context,
	) : SelectionChromeSession {
		private val menu: Menu = MenuBuilder(context).also {
			activity?.menuInflater?.inflate(menuRes, it)
		}

		override var title: String = ""
			private set

		override var actions: List<SelectionChromeAction> = emptyList()
			private set

		fun publish() {
			title = selectedIds.size.toString()
			prepareMenuVisibility(menu)
			actions = menu.toChromeActions()
			chrome?.refresh()
		}

		override fun onAction(id: Int) {
			val ids = selectedIds
			val manga = selectedManga()
			val finish = { dismiss() }
			if (id == R.id.action_select_all) {
				selectAll()
				return
			}
			val router = (activity as? FragmentActivity)?.router
			val handled = handleCommonMangaQuickAction(
				itemId = id,
				context = context,
				router = router,
				manga = manga,
				finish = finish,
			) || onCustomAction(id, ids, manga, finish)
			if (!handled) {
				// no-op
			}
		}

		override fun dismiss() {
			clear()
		}

		private fun prepareMenuVisibility(menu: Menu) {
			val selected = selectedManga()
			val hasNoLocal = selected.isNotEmpty() && selected.none { it.isLocal }
			val hasSingleSource = selected.map { it.source }.distinct().size == 1
			val isSingle = selectedIds.size == 1
			menu.findItem(R.id.action_save)?.isVisible = hasNoLocal
			menu.findItem(R.id.action_replace_source)?.isVisible = hasNoLocal && hasSingleSource
			menu.findItem(R.id.action_edit_override)?.isVisible = isSingle
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
	val activity = remember(context) { context.findActivity() as? FragmentActivity }
	val chrome = remember(activity) { (activity as? BaseComposeActivity)?.selectionChrome }
	val contentState = rememberUpdatedState(content)
	val actionState = rememberUpdatedState(onCustomAction)

	val state = remember(activity, chrome, menuRes) {
		MangaListSelectionState(
			activity = activity,
			chrome = chrome,
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

fun Menu.toChromeActions(): List<SelectionChromeAction> = buildList {
	for (i in 0 until size()) {
		val item = getItem(i)
		add(
			SelectionChromeAction(
				id = item.itemId,
				title = item.title?.toString().orEmpty(),
				iconRes = item.iconResId(),
				visible = item.isVisible,
				enabled = item.isEnabled,
				showInline = item.wantsInlineAction(),
			),
		)
	}
}

private fun MenuItem.iconResId(): Int = try {
	javaClass.getMethod("getIconResId").invoke(this) as Int
} catch (_: ReflectiveOperationException) {
	0
}

private fun MenuItem.wantsInlineAction(): Boolean {
	if (icon == null && iconResId() == 0) return false
	return try {
		val flags = javaClass.getMethod("getShowAsAction").invoke(this) as Int
		flags and (MenuItem.SHOW_AS_ACTION_IF_ROOM or MenuItem.SHOW_AS_ACTION_ALWAYS) != 0
	} catch (_: ReflectiveOperationException) {
		true
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
