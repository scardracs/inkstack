package org.inkstack.core.ui.util

import android.view.ViewGroup
import android.view.Window
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.ActionBarContextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.inkstack.core.util.ext.getThemeColor
import com.google.android.material.R as materialR

class ActionModeDelegate : OnBackPressedCallback(false) {

	private var activeActionMode: ActionMode? = null
	private var listeners: MutableList<ActionModeListener>? = null
	val isActionModeStarted: Boolean
		get() = activeActionMode != null

	override fun handleOnBackPressed() {
		finishActionMode()
	}

	fun onSupportActionModeStarted(mode: ActionMode, window: Window?) {
		activeActionMode = mode
		isEnabled = true
		listeners?.forEach { it.onActionModeStarted(mode) }
		if (window != null) {
			val ctx = window.context
			val actionModeColor = ColorUtils.compositeColors(
				ContextCompat.getColor(ctx, materialR.color.m3_appbar_overlay_color),
				ctx.getThemeColor(materialR.attr.colorSurface),
			)
			val insets = ViewCompat.getRootWindowInsets(window.decorView)
				?.getInsets(WindowInsetsCompat.Type.systemBars()) ?: return
			window.decorView.findViewById<ActionBarContextView?>(androidx.appcompat.R.id.action_mode_bar)?.apply {
				setBackgroundColor(actionModeColor)
				updateLayoutParams<ViewGroup.MarginLayoutParams> {
					topMargin = 0
				}
				updatePadding(top = insets.top)
			}
		}
	}

	fun onSupportActionModeFinished(mode: ActionMode, window: Window?) {
		activeActionMode = null
		isEnabled = false
		listeners?.forEach { it.onActionModeFinished(mode) }
		window?.decorView?.findViewById<ActionBarContextView?>(androidx.appcompat.R.id.action_mode_bar)?.updatePadding(top = 0)
	}

	fun addListener(listener: ActionModeListener) {
		if (listeners == null) {
			listeners = ArrayList()
		}
		checkNotNull(listeners).add(listener)
	}

	fun removeListener(listener: ActionModeListener) {
		listeners?.remove(listener)
	}

	fun addListener(listener: ActionModeListener, owner: LifecycleOwner) {
		addListener(listener)
		owner.lifecycle.addObserver(ListenerLifecycleObserver(listener))
	}

	fun finishActionMode() {
		activeActionMode?.finish()
	}

	private inner class ListenerLifecycleObserver(
		private val listener: ActionModeListener,
	) : DefaultLifecycleObserver {

		override fun onDestroy(owner: LifecycleOwner) {
			super.onDestroy(owner)
			removeListener(listener)
		}
	}
}
