package org.inkstack.alternatives.ui

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.annotation.UiContext
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.inkstack.R
import org.inkstack.core.model.getTitle
import org.inkstack.core.model.isLocal
import org.inkstack.core.ui.compose.InkstackTheme
import org.inkstack.core.ui.compose.MangaCover
import org.inkstack.parsers.model.Manga

/**
 * Prompt shown when a manga the user just opened is gone from its source (404 / removed).
 */
class MangaUnavailableDialog private constructor(
	private val delegate: AlertDialog,
) : DialogInterface by delegate {

	val isShowing: Boolean
		get() = delegate.isShowing

	fun show() {
		delegate.show()
	}

	class Builder(@UiContext private val context: Context, private val manga: Manga) {

		private var onAlternatives: Runnable? = null

		fun setOnAlternativesClickListener(listener: Runnable) = apply {
			onAlternatives = listener
		}

		fun create(): MangaUnavailableDialog {
			lateinit var dialog: AlertDialog
			val composeView = ComposeView(context).apply {
				setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
				setContent {
					InkstackTheme {
						Surface(shape = MaterialTheme.shapes.extraLarge) {
							MangaUnavailableContent(
								manga = manga,
								onAlternatives = {
									dialog.dismiss()
									onAlternatives?.run()
								},
								onClose = { dialog.dismiss() },
							)
						}
					}
				}
			}
			dialog = MaterialAlertDialogBuilder(context)
				.setView(composeView)
				.create()
			dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
			return MangaUnavailableDialog(dialog)
		}
	}
}

@Composable
private fun MangaUnavailableContent(
	manga: Manga,
	onAlternatives: () -> Unit,
	onClose: () -> Unit,
) {
	val context = LocalContext.current
	val sourceTitle = if (manga.isLocal) null else manga.source.getTitle(context)
	val message = if (sourceTitle != null) {
		stringResource(R.string.manga_unavailable_message, manga.title, sourceTitle)
	} else {
		stringResource(R.string.manga_unavailable_message_no_source, manga.title)
	}

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.verticalScroll(rememberScrollState())
			.padding(horizontal = 24.dp, vertical = 24.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Box {
			MangaCover(
				model = manga.coverUrl,
				contentDescription = manga.title,
				modifier = Modifier
					.width(108.dp)
					.height(152.dp)
					.padding(end = 12.dp, bottom = 12.dp),
			)
			Box(
				modifier = Modifier
					.align(Alignment.BottomEnd)
					.size(40.dp)
					.clip(CircleShape)
					.background(MaterialTheme.colorScheme.errorContainer),
				contentAlignment = Alignment.Center,
			) {
				Icon(
					painter = painterResource(R.drawable.ic_error_small),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onErrorContainer,
					modifier = Modifier.size(22.dp),
				)
			}
		}
		Text(
			text = stringResource(R.string.manga_unavailable_title),
			style = MaterialTheme.typography.headlineSmall,
			textAlign = TextAlign.Center,
			modifier = Modifier.padding(top = 20.dp),
		)
		Text(
			text = message,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			textAlign = TextAlign.Center,
			modifier = Modifier.padding(top = 10.dp),
		)
		Text(
			text = stringResource(R.string.manga_unavailable_hint),
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			textAlign = TextAlign.Center,
			modifier = Modifier.padding(top = 12.dp),
		)
		Button(
			onClick = onAlternatives,
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 24.dp)
				.height(56.dp),
		) {
			Icon(
				painter = painterResource(R.drawable.ic_replace),
				contentDescription = null,
				modifier = Modifier.padding(end = 8.dp),
			)
			Text(stringResource(R.string.manga_unavailable_search_alternatives))
		}
		TextButton(onClick = onClose, modifier = Modifier.padding(top = 4.dp)) {
			Text(stringResource(R.string.close))
		}
	}
}
