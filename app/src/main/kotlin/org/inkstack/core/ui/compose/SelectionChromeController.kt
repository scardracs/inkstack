package org.inkstack.core.ui.compose

import android.content.Context
import android.view.Menu
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
import org.inkstack.core.util.ext.findActivity

/**
 * Id-based multi-select backed by [SelectionChromeHost] and a `mode_*` menu XML.
 */
@Stable
class SelectionChromeController internal constructor(
	private val activity: FragmentActivity?,
	private val chrome: SelectionChromeHost?,
	@MenuRes private val menuRes: Int,
	private val collectAllIds: () -> Set<Long>,
	private val prepareMenu: (Menu, Set<Long>) -> Unit,
	private val handleAction: (itemId: Int, selectedIds: Set<Long>, finish: () -> Unit) -> Boolean,
) {
	var selectedIds by mutableStateOf(emptySet<Long>())
		private set

	val isActive: Boolean
		get() = selectedIds.isNotEmpty()

	private var session: ChromeSession? = null

	fun onItemClick(id: Long, open: () -> Unit) {
		if (selectedIds.isEmpty()) {
			open()
		} else {
			toggle(id)
		}
	}

	fun onItemLongClick(id: Long) {
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
		selectedIds = collectAllIds()
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
		val newSession = ChromeSession(ctx)
		session = newSession
		newSession.publish()
		host.show(newSession)
	}

	private inner class ChromeSession(
		context: Context,
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
			prepareMenu(menu, selectedIds)
			actions = menu.toChromeActions()
			chrome?.refresh()
		}

		override fun onAction(id: Int) {
			if (id == R.id.action_select_all) {
				selectAll()
				return
			}
			handleAction(id, selectedIds) { dismiss() }
		}

		override fun dismiss() {
			clear()
		}
	}
}

@Composable
fun rememberSelectionChromeController(
	@MenuRes menuRes: Int,
	collectAllIds: () -> Set<Long>,
	prepareMenu: (Menu, Set<Long>) -> Unit = { _, _ -> },
	handleAction: (itemId: Int, selectedIds: Set<Long>, finish: () -> Unit) -> Boolean,
): SelectionChromeController {
	val context = LocalContext.current
	val activity = remember(context) { context.findActivity() as? FragmentActivity }
	val chrome = rememberSelectionChromeHost()
	val idsState = rememberUpdatedState(collectAllIds)
	val prepareState = rememberUpdatedState(prepareMenu)
	val actionState = rememberUpdatedState(handleAction)

	val controller = remember(activity, chrome, menuRes) {
		SelectionChromeController(
			activity = activity,
			chrome = chrome,
			menuRes = menuRes,
			collectAllIds = { idsState.value() },
			prepareMenu = { menu, ids -> prepareState.value(menu, ids) },
			handleAction = { itemId, ids, finish -> actionState.value(itemId, ids, finish) },
		)
	}

	DisposableEffect(controller) {
		onDispose { controller.clear() }
	}
	return controller
}
