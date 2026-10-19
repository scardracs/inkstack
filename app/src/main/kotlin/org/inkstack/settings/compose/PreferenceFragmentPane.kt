package org.inkstack.settings.compose

import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import org.inkstack.R
import org.inkstack.core.util.ext.findActivity

/**
 * Hosts a legacy [Fragment] (usually PreferenceFragment) inside Compose while section UIs
 * migrate. Prefer pure Compose section screens when parity is ready.
 */
@Composable
fun PreferenceFragmentPane(
	fragmentClass: Class<out Fragment>,
	args: android.os.Bundle? = null,
	modifier: Modifier = Modifier,
) {
	val context = LocalContext.current
	val activity = remember(context) { context.findActivity() as? FragmentActivity }
	val containerId = remember { R.id.settings_compose_fragment_host }

	AndroidView(
		factory = { ctx ->
			FrameLayout(ctx).apply {
				id = containerId
				layoutParams = FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.MATCH_PARENT,
					FrameLayout.LayoutParams.MATCH_PARENT,
				)
			}
		},
		modifier = modifier.fillMaxSize(),
		update = { /* fragment managed below */ },
	)

	DisposableEffect(activity, fragmentClass, args) {
		val fm = activity?.supportFragmentManager
		if (fm != null) {
			fm.commit {
				setReorderingAllowed(true)
				replace(containerId, fragmentClass, args)
				setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
			}
		}
		onDispose {
			val current = fm?.findFragmentById(containerId) ?: return@onDispose
			fm.commit(allowStateLoss = true) {
				setReorderingAllowed(true)
				remove(current)
			}
		}
	}
}
