package org.inkstack.core.ui.compose

import androidx.annotation.DrawableRes
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.inkstack.R
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.core.util.ext.findActivity

@Stable
data class SelectionChromeAction(
	val id: Int,
	val title: String,
	@DrawableRes val iconRes: Int = 0,
	val visible: Boolean = true,
	val enabled: Boolean = true,
	val showInline: Boolean = false,
)

/**
 * Activity-scoped Compose selection session. No AppCompat ActionMode / View chrome.
 */
@Stable
class SelectionChromeHost {
	var session by mutableStateOf<SelectionChromeSession?>(null)
		private set

	private var revision by mutableIntStateOf(0)

	/** Observed so chrome recomposes when [refresh] is called. */
	val revisionTick: Int
		get() = revision

	fun show(session: SelectionChromeSession) {
		this.session = session
		revision++
	}

	fun refresh() {
		revision++
	}

	fun hide(session: SelectionChromeSession? = this.session) {
		if (session == null || this.session === session) {
			this.session = null
			revision++
		}
	}
}

interface SelectionChromeSession {
	val title: String
	val actions: List<SelectionChromeAction>
	fun onAction(id: Int)
	fun dismiss()
}

@Composable
fun rememberSelectionChromeHost(): SelectionChromeHost? {
	val context = LocalContext.current
	return remember(context) {
		(context.findActivity() as? BaseComposeActivity)?.selectionChrome
	}
}

@Composable
fun rememberSelectionChromeSession(): SelectionChromeSession? {
	val host = rememberSelectionChromeHost() ?: return null
	// Read revision so invalidate/refresh recomposes title & actions.
	@Suppress("UNUSED_VARIABLE")
	val tick = host.revisionTick
	return host.session
}

/**
 * Same glass pill as [org.inkstack.main.ui.compose.MainSearchBar] — closes selection and hosts actions.
 */
@Composable
fun GlassSelectionBar(
	session: SelectionChromeSession,
	modifier: Modifier = Modifier,
) {
	val host = rememberSelectionChromeHost()
	@Suppress("UNUSED_VARIABLE")
	val tick = host?.revisionTick
	BackHandler { session.dismiss() }
	var overflowExpanded by remember { mutableStateOf(false) }
	// Opaque glass so luminance-based content color matches the painted fill
	// (translucent pills blend with covers and defeat contrast).
	val content = inkstackGlassContentColor().copy(alpha = 1f)
	val menuContent = MaterialTheme.colorScheme.onSurface
	val visible = session.actions.filter { it.visible }
	val inline = visible.filter { it.showInline && it.iconRes != 0 }.take(3)
	val overflow = visible.filterNot { it in inline }

	InkstackGlassSurface(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 8.dp),
		shape = InkstackGlassShape,
		alpha = 1f,
		enabledBlur = false,
	) {
		CompositionLocalProvider(LocalContentColor provides content) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.defaultMinSize(minHeight = 56.dp)
					.padding(horizontal = 4.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.Start,
			) {
				IconButton(onClick = session::dismiss) {
					Icon(
						imageVector = Icons.Filled.Close,
						contentDescription = stringResource(R.string.close),
						tint = content,
					)
				}
				Text(
					text = session.title,
					modifier = Modifier
						.weight(1f)
						.padding(horizontal = 4.dp),
					style = MaterialTheme.typography.titleMedium,
					color = content,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
				inline.forEach { action ->
					IconButton(
						onClick = { session.onAction(action.id) },
						enabled = action.enabled,
					) {
						SelectionActionIcon(action = action, tint = content)
					}
				}
				if (overflow.isNotEmpty()) {
					Box {
						IconButton(onClick = { overflowExpanded = true }) {
							Icon(
								imageVector = Icons.Filled.MoreVert,
								contentDescription = stringResource(R.string.more_options),
								tint = content,
							)
						}
						DropdownMenu(
							expanded = overflowExpanded,
							onDismissRequest = { overflowExpanded = false },
						) {
							overflow.forEach { action ->
								DropdownMenuItem(
									text = { Text(action.title) },
									onClick = {
										overflowExpanded = false
										session.onAction(action.id)
									},
									enabled = action.enabled,
									leadingIcon = {
										// Popup uses the theme surface — not the glass fill.
										SelectionActionIcon(action = action, tint = menuContent)
									},
								)
							}
						}
					}
				}
			}
		}
	}
}

/**
 * Swaps [normal] top bar for [GlassSelectionBar] while a selection session is active.
 * Set [applyStatusBars] when replacing a Material TopAppBar that normally consumes status insets
 * (e.g. [InkstackScaffold]). Leave false when the caller already pads for the status bar
 * (e.g. main search slot).
 */
@Composable
fun SelectionAwareTopBar(
	modifier: Modifier = Modifier,
	applyStatusBars: Boolean = false,
	normal: @Composable () -> Unit,
) {
	val session = rememberSelectionChromeSession()
	if (session != null) {
		if (applyStatusBars) {
			Column(
				modifier = modifier
					.fillMaxWidth()
					.windowInsetsPadding(WindowInsets.statusBars),
			) {
				GlassSelectionBar(session = session)
			}
		} else {
			GlassSelectionBar(session = session, modifier = modifier)
		}
	} else {
		normal()
	}
}

@Composable
private fun SelectionActionIcon(
	action: SelectionChromeAction,
	tint: Color,
) {
	if (action.iconRes == 0) return
	// vectorResource ignores XML android:tint (?colorControlNormal), so our glass tint stays bianco.
	val image: ImageVector = ImageVector.vectorResource(action.iconRes)
	Icon(
		imageVector = image,
		contentDescription = action.title,
		tint = tint,
	)
}
