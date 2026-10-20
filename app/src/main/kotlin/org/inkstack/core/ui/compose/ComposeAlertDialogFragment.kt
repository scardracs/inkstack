package org.inkstack.core.ui.compose

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.DialogFragment

/**
 * DialogFragment host for Material3 Compose dialogs/content.
 * Keeps AppRouter `.show()` launch APIs while the UI is Compose.
 */
abstract class ComposeAlertDialogFragment : DialogFragment() {

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return Dialog(requireContext(), theme).also { dialog ->
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
			dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
		}
	}

	final override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View = ComposeView(requireContext()).apply {
		layoutParams = ViewGroup.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT,
		)
		setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
		setContent {
			InkstackTheme {
				DialogContent(onDismissRequest = { dismissAllowingStateLoss() })
			}
		}
	}

	@Composable
	protected abstract fun DialogContent(onDismissRequest: () -> Unit)
}
