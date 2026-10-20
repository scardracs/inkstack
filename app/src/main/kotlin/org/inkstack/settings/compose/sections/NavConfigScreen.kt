package org.inkstack.settings.compose.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.inkstack.R
import org.inkstack.settings.nav.NavConfigViewModel
import org.inkstack.settings.nav.model.NavItemAddModel
import org.inkstack.settings.nav.model.NavItemConfigModel

@Composable
fun NavConfigScreen(
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: NavConfigViewModel = hiltViewModel(),
) {
	val content by viewModel.content.collectAsState()
	var showAdd by remember { mutableStateOf(false) }

	SettingsSectionScaffold(
		titleRes = R.string.main_screen_sections,
		onBack = onBack,
		modifier = modifier,
	) {
		items(content.size, key = { index ->
			when (val item = content[index]) {
				is NavItemConfigModel -> item.item.name
				is NavItemAddModel -> "add"
				else -> "item-$index"
			}
		}) { index ->
			when (val item = content[index]) {
				is NavItemConfigModel -> {
					ListItem(
						headlineContent = { Text(stringResource(item.item.title)) },
						leadingContent = {
							Icon(painter = painterResource(item.item.icon), contentDescription = null)
						},
						trailingContent = {
							IconButton(onClick = { viewModel.removeItem(item.item) }) {
								Icon(Icons.Default.Delete, contentDescription = null)
							}
						},
						modifier = Modifier.fillMaxWidth(),
					)
				}
				is NavItemAddModel -> {
					ListItem(
						headlineContent = { Text(stringResource(R.string.add)) },
						leadingContent = {
							Icon(Icons.Default.Add, contentDescription = null)
						},
						modifier = Modifier
							.fillMaxWidth()
							.clickable(enabled = item.canAdd) { showAdd = true },
					)
				}
				else -> Unit
			}
		}
	}

	if (showAdd) {
		AlertDialog(
			onDismissRequest = { showAdd = false },
			title = { Text(stringResource(R.string.add)) },
			text = {
				androidx.compose.foundation.layout.Column {
					viewModel.availableItems.forEach { navItem ->
						ListItem(
							headlineContent = { Text(stringResource(navItem.title)) },
							leadingContent = {
								Icon(painter = painterResource(navItem.icon), contentDescription = null)
							},
							modifier = Modifier
								.fillMaxWidth()
								.clickable {
									viewModel.addItem(navItem)
									showAdd = false
								},
						)
					}
				}
			},
			confirmButton = {
				TextButton(onClick = { showAdd = false }) {
					Text(stringResource(android.R.string.cancel))
				}
			},
		)
	}
}
