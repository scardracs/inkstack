package org.inkstack.favourites.ui.categories.select

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.google.android.material.checkbox.MaterialCheckBox
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.core.nav.router
import org.inkstack.core.ui.compose.ComposeAlertDialogFragment
import org.inkstack.core.ui.compose.MangaCover
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.core.util.ext.joinToStringWithLimit
import org.inkstack.favourites.ui.categories.select.model.MangaCategoryItem
import org.inkstack.list.ui.model.EmptyState
import org.inkstack.list.ui.model.LoadingState

@AndroidEntryPoint
class FavoriteDialog : ComposeAlertDialogFragment() {

	private val viewModel by viewModels<FavoriteDialogViewModel>()

	@Composable
	override fun DialogContent(onDismissRequest: () -> Unit) {
		FavoriteDialogContent(
			viewModel = viewModel,
			onDismissRequest = onDismissRequest,
			onManage = {
				router.openFavoriteCategories()
				onDismissRequest()
			},
		)
	}
}

@Composable
private fun FavoriteDialogContent(
	viewModel: FavoriteDialogViewModel,
	onDismissRequest: () -> Unit,
	onManage: () -> Unit,
) {
	val context = LocalContext.current
	val content by viewModel.content.collectAsState()

	LaunchedEffect(viewModel) {
		viewModel.onError.collectLatest { event ->
			event?.consume { error ->
				Toast.makeText(context, error.getDisplayMessage(context.resources), Toast.LENGTH_SHORT).show()
			}
		}
	}

	val title = remember(viewModel.manga) {
		viewModel.manga.joinToStringWithLimit(context, 92) { it.title }
	}

	AlertDialog(
		onDismissRequest = onDismissRequest,
		title = null,
		text = {
			Column(modifier = Modifier.fillMaxWidth()) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(bottom = 8.dp),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(12.dp),
				) {
					viewModel.manga.firstOrNull()?.let { manga ->
						MangaCover(
							model = manga.coverUrl,
							contentDescription = manga.title,
							modifier = Modifier
								.width(56.dp)
								.heightIn(max = 84.dp),
						)
					}
					Text(
						text = title,
						style = MaterialTheme.typography.bodyLarge,
						modifier = Modifier.weight(1f),
					)
				}
				when {
					content.any { it is LoadingState } -> {
						CircularProgressIndicator(
							modifier = Modifier
								.align(Alignment.CenterHorizontally)
								.padding(16.dp),
						)
					}
					content.any { it is EmptyState } -> {
						Text(
							text = stringResource(R.string.empty_favourite_categories),
							modifier = Modifier.padding(16.dp),
						)
					}
					else -> {
						LazyColumn(
							modifier = Modifier
								.fillMaxWidth()
								.heightIn(max = 360.dp),
						) {
							items(
								items = content.filterIsInstance<MangaCategoryItem>(),
								key = { it.category.id },
							) { item ->
								CategoryCheckRow(
									item = item,
									onClick = {
										viewModel.setChecked(
											item.category.id,
											item.checkedState != MaterialCheckBox.STATE_CHECKED,
										)
									},
								)
							}
						}
					}
				}
			}
		},
		confirmButton = {
			TextButton(onClick = onDismissRequest) {
				Text(stringResource(R.string.done))
			}
		},
		dismissButton = {
			TextButton(onClick = onManage) {
				Text(stringResource(R.string.manage))
			}
		},
	)
}

@Composable
private fun CategoryCheckRow(
	item: MangaCategoryItem,
	onClick: () -> Unit,
) {
	val state = when (item.checkedState) {
		MaterialCheckBox.STATE_CHECKED -> ToggleableState.On
		MaterialCheckBox.STATE_INDETERMINATE -> ToggleableState.Indeterminate
		else -> ToggleableState.Off
	}
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(horizontal = 4.dp, vertical = 8.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		TriStateCheckbox(state = state, onClick = onClick)
		Text(
			text = item.category.title,
			style = MaterialTheme.typography.bodyLarge,
			modifier = Modifier
				.weight(1f)
				.padding(start = 4.dp),
		)
		if (item.isTrackerEnabled && item.category.isTrackingEnabled) {
			Icon(
				painter = painterResource(R.drawable.ic_notification),
				contentDescription = null,
				modifier = Modifier.size(18.dp),
			)
		}
		if (!item.category.isVisibleInLibrary) {
			Icon(
				painter = painterResource(R.drawable.ic_eye_off),
				contentDescription = null,
				modifier = Modifier.size(18.dp),
			)
		}
	}
}
